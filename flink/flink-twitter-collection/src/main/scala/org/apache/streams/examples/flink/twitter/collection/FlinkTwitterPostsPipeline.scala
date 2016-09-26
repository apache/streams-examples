package org.apache.streams.examples.flink.twitter.collection

import java.util.concurrent.TimeUnit

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.util.concurrent.Uninterruptibles
import com.peoplepattern.streams.pdb.pipelines.FlinkStreamingConfiguration
import com.peoplepattern.streams.pdb.flink.{FlinkBase, FlinkUtil}
import com.peoplepattern.streams.pipelines.pdb.TwitterPostsPipelineConfiguration
import com.peoplepattern.streams.twitter.collection.FlinkTwitterPostsPipeline.LOGGER
import org.apache.flink.api.common.functions.{FlatMapFunction, RichFlatMapFunction}
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.scala.{ExecutionEnvironment, _}
import org.apache.flink.core.fs.FileSystem
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.function.AllWindowFunction
import org.apache.flink.streaming.api.scala.{AllWindowedStream, DataStream, KeyedStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.ContinuousEventTimeTrigger
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow
import org.apache.flink.streaming.connectors.fs.RollingSink
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector
import org.apache.streams.config.{ComponentConfigurator, StreamsConfigurator}
import org.apache.streams.core.StreamsDatum
import org.apache.streams.examples.flink.FlinkBase
import org.apache.streams.examples.flink.twitter.TwitterPostsPipelineConfiguration
import org.apache.streams.flink.FlinkStreamingConfiguration
import org.apache.streams.hdfs.HdfsConfiguration
import org.apache.streams.jackson.StreamsJacksonMapper
import org.apache.streams.twitter.TwitterUserInformationConfiguration
import org.apache.streams.twitter.pojo.{Tweet, User}
import org.apache.streams.twitter.provider.{TwitterTimelineProvider, TwitterUserInformationProvider}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._

/**
  * Created by sblackmon on 7/29/15.
  */
object FlinkTwitterPostsPipeline extends FlinkBase {

  val STREAMS_ID: String = "FlinkTwitterPostsPipeline"

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FlinkTwitterPostsPipeline])
  private val MAPPER: ObjectMapper = StreamsJacksonMapper.getInstance()

  override def main(args: Array[String]) = {
    super.main(args)
    val jobConfig = new ComponentConfigurator[TwitterPostsPipelineConfiguration](classOf[TwitterPostsPipelineConfiguration]).detectConfiguration(typesafe)
    if( setup(jobConfig) == false ) System.exit(1)
    val pipeline: FlinkTwitterPostsPipeline = new FlinkTwitterPostsPipeline(jobConfig)
    val thread = new Thread(pipeline)
    thread.start()
    thread.join()
  }

  def setup(jobConfig: TwitterPostsPipelineConfiguration): Boolean =  {

    LOGGER.info("TwitterPostsPipelineConfiguration: " + jobConfig)

    if( jobConfig == null ) {
      LOGGER.error("jobConfig is null!")
      System.err.println("jobConfig is null!")
      return false
    }

    if( jobConfig.getSource == null ) {
      LOGGER.error("jobConfig.getSource is null!")
      System.err.println("jobConfig.getSource is null!")
      return false
    }

    if( jobConfig.getDestination == null ) {
      LOGGER.error("jobConfig.getDestination is null!")
      System.err.println("jobConfig.getDestination is null!")
      return false
    }

    if( jobConfig.getTwitter == null ) {
      LOGGER.error("jobConfig.getTwitter is null!")
      System.err.println("jobConfig.getTwitter is null!")
      return false
    }

    return true

  }

}

class FlinkTwitterPostsPipeline(config: TwitterPostsPipelineConfiguration = new ComponentConfigurator[TwitterPostsPipelineConfiguration](classOf[TwitterPostsPipelineConfiguration]).detectConfiguration(StreamsConfigurator.getConfig)) extends Runnable with java.io.Serializable {

  import FlinkTwitterPostsPipeline._

  override def run(): Unit = {

    val env: StreamExecutionEnvironment = streamEnvironment(MAPPER.convertValue(config, classOf[FlinkStreamingConfiguration]))

    env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime);
    env.setNumberOfExecutionRetries(0)

    val inPath = buildReaderPath(config.getSource)

    val outPath = buildWriterPath(config.getDestination)

    //val inProps = buildKafkaProps(config.getSourceTopic)

    val ids: DataStream[String] = env.readTextFile(inPath).setParallelism(10).name("ids")

    //val idTopicIn = new KafkaSink()

//    val idTopicOut : DataStream[String] = env.addSource[String](
//      new org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09(config.getSourceTopic.getTopic, new SimpleStringSchema(),
//        inProps));

    val keyed_ids: KeyedStream[String, Int] = env.readTextFile(inPath).setParallelism(10).name("keyed_ids").keyBy( id => (id.hashCode % 100).abs )

    // these datums contain 'Tweet' objects
    val tweetDatums: DataStream[StreamsDatum] =
      keyed_ids.flatMap(new postCollectorFlatMapFunction).setParallelism(10).name("tweetDatums")

    val tweets: DataStream[Tweet] = tweetDatums
      .map(datum => datum.getDocument.asInstanceOf[Tweet]).name("tweets")

    val jsons: DataStream[String] = tweets
      .map(tweet => {
        val MAPPER = StreamsJacksonMapper.getInstance
        MAPPER.writeValueAsString(tweet)
      }).name("json")

    if( config.getTest == false )
      jsons.addSink(new RollingSink[String](outPath)).setParallelism(3).name("hdfs")
    else
      jsons.writeAsText(outPath,FileSystem.WriteMode.OVERWRITE)
        .setParallelism(env.getParallelism);

    // if( test == true ) jsons.print();

    env.execute("FlinkTwitterPostsPipeline")
  }

  class postCollectorFlatMapFunction extends RichFlatMapFunction[String, StreamsDatum] with Serializable {
    override def flatMap(input: String, out: Collector[StreamsDatum]): Unit = {
        collectPosts(input, out)
    }
    def collectPosts(id : String, out : Collector[StreamsDatum]) = {
      val twitterConfiguration = config.getTwitter
      val twitProvider: TwitterTimelineProvider =
        new TwitterTimelineProvider(
          twitterConfiguration.withInfo(List(FlinkUtil.toProviderId(id))).withMaxItems(200l)
        )
      twitProvider.prepare(twitProvider)
      twitProvider.startStream()
      var iterator: Iterator[StreamsDatum] = null
      do {
        Uninterruptibles.sleepUninterruptibly(config.getProviderWaitMs, TimeUnit.MILLISECONDS)
        twitProvider.readCurrent().iterator().toList.map(out.collect(_))
      } while( twitProvider.isRunning )
    }
  }


}