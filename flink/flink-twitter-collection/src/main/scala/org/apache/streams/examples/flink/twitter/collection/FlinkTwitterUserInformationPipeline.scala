package org.apache.streams.examples.flink.twitter.collection

import java.util.concurrent.TimeUnit

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.{Preconditions, Strings}
import org.apache.flink.core.fs.FileSystem
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.function.{AllWindowFunction, WindowFunction}
import org.apache.flink.streaming.api.windowing.assigners.{GlobalWindows, TumblingEventTimeWindows}
import com.google.common.util.concurrent.Uninterruptibles
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.streaming.api.scala.{AllWindowedStream, DataStream, KeyedStream, StreamExecutionEnvironment, WindowedStream}
import org.apache.flink.streaming.api.windowing.windows.{GlobalWindow, TimeWindow, Window}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.connectors.fs.RollingSink
import org.apache.flink.util.Collector
import org.apache.streams.config.{ComponentConfigurator, StreamsConfigurator}
import org.apache.streams.core.StreamsDatum
import org.apache.streams.examples.flink.FlinkBase
import org.apache.streams.examples.flink.twitter.TwitterUserInformationPipelineConfiguration
import org.apache.streams.flink.FlinkStreamingConfiguration
import org.apache.streams.jackson.StreamsJacksonMapper
import org.apache.streams.twitter.pojo.User
import org.apache.streams.twitter.provider.TwitterUserInformationProvider
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._

/**
  * Created by sblackmon on 3/15/16.
  */
object FlinkTwitterUserInformationPipeline extends FlinkBase {

  val STREAMS_ID: String = "FlinkTwitterUserInformationPipeline"

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FlinkTwitterUserInformationPipeline])
  private val MAPPER: ObjectMapper = StreamsJacksonMapper.getInstance()

  override def main(args: Array[String]) = {
    super.main(args)
    val jobConfig = new ComponentConfigurator[TwitterUserInformationPipelineConfiguration](classOf[TwitterUserInformationPipelineConfiguration]).detectConfiguration(typesafe)
    if( setup(jobConfig) == false ) System.exit(1)
    val pipeline: FlinkTwitterUserInformationPipeline = new FlinkTwitterUserInformationPipeline(jobConfig)
    val thread = new Thread(pipeline)
    thread.start()
    thread.join()
  }

  def setup(jobConfig: TwitterUserInformationPipelineConfiguration): Boolean =  {

    LOGGER.info("TwitterFollowingPipelineConfiguration: " + jobConfig)

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

    Preconditions.checkNotNull(jobConfig.getTwitter.getOauth)
    Preconditions.checkArgument(!Strings.isNullOrEmpty(jobConfig.getTwitter.getOauth.getAccessToken))
    Preconditions.checkArgument(!Strings.isNullOrEmpty(jobConfig.getTwitter.getOauth.getAccessTokenSecret))
    Preconditions.checkArgument(!Strings.isNullOrEmpty(jobConfig.getTwitter.getOauth.getConsumerKey))
    Preconditions.checkArgument(!Strings.isNullOrEmpty(jobConfig.getTwitter.getOauth.getConsumerSecret))

    return true

  }

}

class FlinkTwitterUserInformationPipeline(config: TwitterUserInformationPipelineConfiguration = new ComponentConfigurator[TwitterUserInformationPipelineConfiguration](classOf[TwitterUserInformationPipelineConfiguration]).detectConfiguration(StreamsConfigurator.getConfig)) extends Runnable with java.io.Serializable {

  import FlinkTwitterUserInformationPipeline._

  override def run(): Unit = {

    val env: StreamExecutionEnvironment = streamEnvironment(MAPPER.convertValue(config, classOf[FlinkStreamingConfiguration]))

    env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime);
    env.setNumberOfExecutionRetries(0)

    val inPath = buildReaderPath(config.getSource)

    val outPath = buildWriterPath(config.getDestination)

    val ids: DataStream[String] = env.readTextFile(inPath).setParallelism(10).name("ids")

    val keyed_ids: KeyedStream[String, Int] = ids.name("keyed_ids").keyBy( id => (id.hashCode % 100).abs )

    val idWindows: WindowedStream[String, Int, GlobalWindow] = keyed_ids.countWindow(100)

    val idLists: DataStream[List[String]] = idWindows.apply[List[String]] (new idListWindowFunction()).name("idLists")

    val userDatums: DataStream[StreamsDatum] = idLists.flatMap(new profileCollectorFlatMapFunction).setParallelism(10).name("userDatums")

    val user: DataStream[User] = userDatums.map(datum => datum.getDocument.asInstanceOf[User]).name("users")

    val jsons: DataStream[String] = user
      .map(user => {
        val MAPPER = StreamsJacksonMapper.getInstance
        MAPPER.writeValueAsString(user)
      }).name("jsons")

    if( config.getTest == false )
      jsons.addSink(new RollingSink[String](outPath)).setParallelism(3).name("hdfs")
    else
      jsons.writeAsText(outPath,FileSystem.WriteMode.OVERWRITE)
        .setParallelism(env.getParallelism);

    LOGGER.info("StreamExecutionEnvironment: {}", env.toString )

    env.execute("FlinkTwitterUserInformationPipeline")
  }

  class idListWindowFunction extends WindowFunction[String, List[String], Int, GlobalWindow] {
    override def apply(key: Int, window: GlobalWindow, input: Iterable[String], out: Collector[List[String]]): Unit = {if( input.size > 0 )
        out.collect(input.map(id => toProviderId(id)).toList)
    }
  }

  class profileCollectorFlatMapFunction extends RichFlatMapFunction[List[String], StreamsDatum] with Serializable {
    override def flatMap(input: List[String], out: Collector[StreamsDatum]): Unit = {
      collectProfiles(input, out)
    }
    def collectProfiles(ids : List[String], out : Collector[StreamsDatum]) = {
      val twitterConfiguration = config.getTwitter
      val twitProvider: TwitterUserInformationProvider =
        new TwitterUserInformationProvider(
          twitterConfiguration.withInfo(ids)
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
