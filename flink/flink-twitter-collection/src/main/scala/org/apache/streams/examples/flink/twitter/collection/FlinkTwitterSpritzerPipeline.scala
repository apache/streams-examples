package org.apache.streams.examples.flink.twitter.collection

import java.util.concurrent.TimeUnit

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.{Preconditions, Strings}
import com.google.common.util.concurrent.Uninterruptibles
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.FileSystem
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}
import org.apache.flink.streaming.api.scala.{DataStream, KeyedStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.fs.RollingSink
import org.apache.streams.config.{ComponentConfigurator, StreamsConfigurator}
import org.apache.streams.core.StreamsDatum
import org.apache.streams.examples.flink.FlinkBase
import org.apache.streams.examples.flink.twitter.TwitterSpritzerPipelineConfiguration
import org.apache.streams.flink.FlinkStreamingConfiguration
import org.apache.streams.jackson.StreamsJacksonMapper
import org.apache.streams.twitter.TwitterStreamConfiguration
import org.apache.streams.twitter.provider.TwitterStreamProvider
import org.slf4j.{Logger, LoggerFactory}
import org.apache.flink.api.scala._

import scala.collection.JavaConversions._

/**
  * Created by sblackmon on 7/29/15.
  */
object FlinkTwitterSpritzerPipeline extends FlinkBase {

  val STREAMS_ID: String = "FlinkTwitterSpritzerPipeline"

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FlinkTwitterPostsPipeline])
  private val MAPPER: ObjectMapper = StreamsJacksonMapper.getInstance()

  override def main(args: Array[String]) = {
    super.main(args)
    val jobConfig = new ComponentConfigurator(classOf[TwitterSpritzerPipelineConfiguration]).detectConfiguration(typesafe)
    if( setup(jobConfig) == false ) System.exit(1)
    val pipeline: FlinkTwitterSpritzerPipeline = new FlinkTwitterSpritzerPipeline(jobConfig)
    val thread = new Thread(pipeline)
    thread.start()
    thread.join()
  }

  def setup(jobConfig: TwitterSpritzerPipelineConfiguration): Boolean =  {

    LOGGER.info("TwitterSpritzerPipelineConfiguration: " + jobConfig)

    if( jobConfig == null ) {
      LOGGER.error("jobConfig is null!")
      System.err.println("jobConfig is null!")
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

class FlinkTwitterSpritzerPipeline(config: TwitterSpritzerPipelineConfiguration = new ComponentConfigurator(classOf[TwitterSpritzerPipelineConfiguration]).detectConfiguration(StreamsConfigurator.getConfig)) extends Runnable with java.io.Serializable {

  import FlinkTwitterSpritzerPipeline._

  override def run(): Unit = {

    val env: StreamExecutionEnvironment = streamEnvironment(MAPPER.convertValue(config, classOf[FlinkStreamingConfiguration]))

    env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime);
    env.setNumberOfExecutionRetries(0)

    val outPath = buildWriterPath(config.getDestination)

    val streamSource : DataStream[String] = env.addSource(new SpritzerSource(config.getTwitter));

    if( config.getTest == false )
      streamSource.addSink(new RollingSink[String](outPath)).setParallelism(3).name("hdfs")
    else
      streamSource.writeAsText(outPath,FileSystem.WriteMode.OVERWRITE)
        .setParallelism(env.getParallelism);

    // if( test == true ) jsons.print();

    env.execute("FlinkTwitterPostsPipeline")
  }

  class SpritzerSource(sourceConfig: TwitterStreamConfiguration) extends RichSourceFunction[String] with Serializable {

    var twitProvider: TwitterStreamProvider = _

    @throws[Exception]
    override def open(parameters: Configuration): Unit = {
      twitProvider = new TwitterStreamProvider( sourceConfig )
      twitProvider.prepare(twitProvider)
      twitProvider.startStream()
    }

    override def run(ctx: SourceFunction.SourceContext[String]): Unit = {
      var iterator: Iterator[StreamsDatum] = null
      do {
        Uninterruptibles.sleepUninterruptibly(config.getProviderWaitMs, TimeUnit.MILLISECONDS)
        iterator = twitProvider.readCurrent().iterator()
        iterator.toList.map(datum => ctx.collect(datum.getDocument.asInstanceOf[String]))
      } while( twitProvider.isRunning )
    }

    override def cancel(): Unit = {
      twitProvider.cleanUp()
    }

    @throws[Exception]
    override def close(): Unit = {
      twitProvider.cleanUp()
    }
  }


}
