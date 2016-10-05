package org.apache.streams.examples.flink.twitter.collection

import java.util.concurrent.TimeUnit

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.{Preconditions, Strings}
import com.google.common.util.concurrent.Uninterruptibles
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.core.fs.FileSystem
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.{DataStream, KeyedStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.fs.RollingSink
import org.apache.flink.util.Collector
import org.apache.streams.config.{ComponentConfigurator, StreamsConfigurator}
import org.apache.streams.core.StreamsDatum
import org.apache.streams.jackson.StreamsJacksonMapper
import org.apache.streams.twitter.TwitterFollowingConfiguration
import org.apache.streams.twitter.pojo.Follow
import org.apache.streams.twitter.provider.TwitterFollowingProvider
import org.slf4j.{Logger, LoggerFactory}
import org.apache.streams.examples.flink.FlinkBase
import org.apache.streams.examples.flink.twitter.TwitterFollowingPipelineConfiguration
import org.apache.streams.flink.{FlinkStreamingConfiguration, StreamsFlinkConfiguration}
import org.apache.flink.api.scala._

import scala.collection.JavaConversions._

/**
 * Created by sblackmon on 4/20/16.
 */
/**
 * Created by sblackmon on 3/15/16.
 */
object FlinkTwitterFollowingPipeline extends FlinkBase {

    val STREAMS_ID: String = "FlinkTwitterFollowingPipeline"

    private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FlinkTwitterUserInformationPipeline])
    private val MAPPER: ObjectMapper = StreamsJacksonMapper.getInstance()

    override def main(args: Array[String]) = {
    super.main(args)
    val jobConfig = new ComponentConfigurator[TwitterFollowingPipelineConfiguration](classOf[TwitterFollowingPipelineConfiguration]).detectConfiguration(typesafe)
    if( setup(jobConfig) == false ) System.exit(1)
    val pipeline: FlinkTwitterFollowingPipeline = new FlinkTwitterFollowingPipeline(jobConfig)
    val thread = new Thread(pipeline)
    thread.start()
    thread.join()
    }

    def setup(jobConfig: TwitterFollowingPipelineConfiguration): Boolean =  {

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

class FlinkTwitterFollowingPipeline(config: TwitterFollowingPipelineConfiguration = new ComponentConfigurator[TwitterFollowingPipelineConfiguration](classOf[TwitterFollowingPipelineConfiguration]).detectConfiguration(StreamsConfigurator.getConfig)) extends Runnable with java.io.Serializable {

    import FlinkTwitterFollowingPipeline._

    override def run(): Unit = {

        val env: StreamExecutionEnvironment = streamEnvironment(MAPPER.convertValue(config, classOf[FlinkStreamingConfiguration]))

        env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime);
        env.setNumberOfExecutionRetries(0)

        val inPath = buildReaderPath(config.getSource)

        val outPath = buildWriterPath(config.getDestination)

        val keyed_ids: KeyedStream[String, Int] = env.readTextFile(inPath).setParallelism(10).keyBy( id => (id.hashCode % 100).abs )

        // these datums contain 'Follow' objects
        val followDatums: DataStream[StreamsDatum] =
            keyed_ids.flatMap(new FollowingCollectorFlatMapFunction(config.getTwitter)).setParallelism(10)

        val follows: DataStream[Follow] = followDatums
          .map(datum => datum.getDocument.asInstanceOf[Follow])

        val jsons: DataStream[String] = follows
          .map(follow => {
              val MAPPER = StreamsJacksonMapper.getInstance
              MAPPER.writeValueAsString(follow)
          })

        if( config.getTest == false )
            jsons.addSink(new RollingSink[String](outPath)).setParallelism(3)
        else
            jsons.writeAsText(outPath,FileSystem.WriteMode.OVERWRITE)
              .setParallelism(env.getParallelism);

        // if( test == true ) jsons.print();

        env.execute(STREAMS_ID)
    }

    class FollowingCollectorFlatMapFunction(
                                             twitterConfiguration : TwitterFollowingConfiguration = new ComponentConfigurator[TwitterFollowingConfiguration](classOf[TwitterFollowingConfiguration]).detectConfiguration(StreamsConfigurator.getConfig.getConfig("twitter")),
                                             flinkConfiguration : StreamsFlinkConfiguration = new ComponentConfigurator[StreamsFlinkConfiguration](classOf[StreamsFlinkConfiguration]).detectConfiguration(StreamsConfigurator.getConfig)
                                           ) extends RichFlatMapFunction[String, StreamsDatum] with Serializable {

        override def flatMap(input: String, out: Collector[StreamsDatum]): Unit = {
            collectConnections(input, out)
        }

        def collectConnections(id : String, out : Collector[StreamsDatum]) = {
            val twitProvider: TwitterFollowingProvider =
                new TwitterFollowingProvider(
                    twitterConfiguration.withIdsOnly(true).withInfo(List(toProviderId(id))).withMaxItems(5000l).asInstanceOf[TwitterFollowingConfiguration]
                )
            twitProvider.prepare(twitProvider)
            twitProvider.startStream()
            var iterator: Iterator[StreamsDatum] = null
            do {
                Uninterruptibles.sleepUninterruptibly(flinkConfiguration.getProviderWaitMs, TimeUnit.MILLISECONDS)
                twitProvider.readCurrent().iterator().toList.map(out.collect(_))
            } while( twitProvider.isRunning )
        }
    }

}