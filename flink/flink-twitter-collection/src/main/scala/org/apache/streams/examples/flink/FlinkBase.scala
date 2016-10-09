package org.apache.streams.examples.flink

import java.net.MalformedURLException

import com.google.common.base.Strings
import com.typesafe.config.Config
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.streams.config.{ComponentConfigurator, StreamsConfigurator}
import org.apache.streams.flink.{FlinkBatchConfiguration, FlinkStreamingConfiguration, StreamsFlinkConfiguration}
import org.apache.streams.hdfs.{HdfsConfiguration, HdfsReaderConfiguration, HdfsWriterConfiguration}
import org.apache.streams.jackson.StreamsJacksonMapper
import org.slf4j.LoggerFactory

trait FlinkBase {

  private val BASELOGGER = LoggerFactory.getLogger("FlinkBase")
  private val MAPPER = StreamsJacksonMapper.getInstance()

  var configUrl : String = _
  var typesafe : Config = _
  var streamsConfig = StreamsConfigurator.detectConfiguration(StreamsConfigurator.getConfig)
  var streamsFlinkConfiguration: StreamsFlinkConfiguration = _

  var executionEnvironment: ExecutionEnvironment = _
  var streamExecutionEnvironment: StreamExecutionEnvironment = _

  /*
   Basic stuff for every flink job
   */
  def main(args: Array[String]): Unit = {
    // if only one argument, use it as the config URL
    if( args.size > 0 ) {
      BASELOGGER.info("Args: {}", args)
      configUrl = args(0)
      setup(configUrl)
    }

  }

  def setup(configUrl : String): Boolean =  {
    BASELOGGER.info("StreamsConfigurator.config: {}", StreamsConfigurator.config)
    if( !Strings.isNullOrEmpty(configUrl)) {
      BASELOGGER.info("StreamsConfigurator.resolveConfig(configUrl): {}", StreamsConfigurator.resolveConfig(configUrl))
      try {
        typesafe = StreamsConfigurator.resolveConfig(configUrl).withFallback(StreamsConfigurator.config).resolve()
      } catch {
        case mue: MalformedURLException => {
          BASELOGGER.error("Invalid Configuration URL: ", mue)
          return false
        }
        case e: Exception => {
          BASELOGGER.error("Invalid Configuration URL: ", e)
          return false
        }
      }
    }
    else {
      typesafe = StreamsConfigurator.getConfig
    }

    return setup(typesafe)

  }

  def setup(typesafe : Config): Boolean =  {
    this.typesafe = typesafe

    BASELOGGER.info("Typesafe Config: {}", typesafe)

    if( this.typesafe.getString("mode").equals("streaming")) {
      val streamingConfiguration: FlinkStreamingConfiguration =
        new ComponentConfigurator[FlinkStreamingConfiguration](classOf[FlinkStreamingConfiguration]).detectConfiguration(typesafe)
      return setupStreaming(streamingConfiguration)
    } else if( this.typesafe.getString("mode").equals("batch")) {
      val batchConfiguration: FlinkBatchConfiguration =
        new ComponentConfigurator[FlinkBatchConfiguration](classOf[FlinkBatchConfiguration]).detectConfiguration(typesafe)
      return setupBatch(batchConfiguration)
    } else {
      return false;
    }
  }

//  def setup(typesafe: Config): Boolean =  {
//
//    val streamsConfig = StreamsConfigurator.detectConfiguration(typesafe)
//
//    this.streamsConfig = streamsConfig
//
//    BASELOGGER.info("Streams Config: " + streamsConfig)
//
//    setup(streamsConfig)
//  }

  def setupStreaming(streamingConfiguration: FlinkStreamingConfiguration): Boolean = {

    BASELOGGER.info("FsStreamingFlinkConfiguration: " + streamingConfiguration)

    this.streamsFlinkConfiguration = streamingConfiguration

    if( streamsFlinkConfiguration == null) return false

    if( streamExecutionEnvironment == null )
      streamExecutionEnvironment = streamEnvironment(streamingConfiguration)

    return false

  }

  def setupBatch(batchConfiguration: FlinkBatchConfiguration): Boolean =  {

    BASELOGGER.info("FsBatchFlinkConfiguration: " + batchConfiguration)

    this.streamsFlinkConfiguration = batchConfiguration

    if( streamsFlinkConfiguration == null) return false

    if( executionEnvironment == null )
      executionEnvironment = batchEnvironment(batchConfiguration)

    return true

  }

  def batchEnvironment(config: FlinkBatchConfiguration = new FlinkBatchConfiguration()) : ExecutionEnvironment = {
    if (config.getTest == false && config.getLocal == false) {
      val env = ExecutionEnvironment.getExecutionEnvironment
      return env
    } else {
      val env = ExecutionEnvironment.createLocalEnvironment(config.getParallel.toInt)
      return env
    }
  }

  def streamEnvironment(config: FlinkStreamingConfiguration = new FlinkStreamingConfiguration()) : StreamExecutionEnvironment = {
    if( config.getTest == false && config.getLocal == false) {
      val env = StreamExecutionEnvironment.getExecutionEnvironment

      env.setRestartStrategy(RestartStrategies.noRestart());

      // start a checkpoint every hour
      env.enableCheckpointing(config.getCheckpointIntervalMs)

      env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)

      // checkpoints have to complete within five minutes, or are discarded
      env.getCheckpointConfig.setCheckpointTimeout(config.getCheckpointTimeoutMs)

      // allow only one checkpoint to be in progress at the same time
      env.getCheckpointConfig.setMaxConcurrentCheckpoints(1)

      return env
    }

    else return StreamExecutionEnvironment.createLocalEnvironment(config.getParallel.toInt)
  }

  def buildReaderPath(configObject: HdfsReaderConfiguration) : String = {
    var inPathBuilder : String = ""
    if (configObject.getScheme.equals(HdfsConfiguration.Scheme.FILE)) {
      inPathBuilder = configObject.getPath + "/" + configObject.getReaderPath
    }
    else if (configObject.getScheme.equals(HdfsConfiguration.Scheme.HDFS)) {
      inPathBuilder = configObject.getScheme + "://" + configObject.getHost + ":" + configObject.getPort + "/" + configObject.getPath + "/" + configObject.getReaderPath
    }
    else if (configObject.getScheme.toString.equals("s3")) {
      inPathBuilder = configObject.getScheme + "://" + configObject.getPath + "/" + configObject.getReaderPath
    } else {
      throw new Exception("scheme not recognized: " + configObject.getScheme)
    }
    return inPathBuilder
  }

  def buildWriterPath(configObject: HdfsWriterConfiguration) : String = {
    var outPathBuilder : String = ""
    if( configObject.getScheme.equals(HdfsConfiguration.Scheme.FILE)) {
      outPathBuilder = configObject.getPath + "/" + configObject.getWriterPath
    }
    else if( configObject.getScheme.equals(HdfsConfiguration.Scheme.HDFS)) {
      outPathBuilder = configObject.getScheme + "://" + configObject.getHost + ":" + configObject.getPort + "/" + configObject.getPath + "/" + configObject.getWriterPath
    }
    else if( configObject.getScheme.toString.equals("s3")) {
      outPathBuilder = configObject.getScheme + "://" + configObject.getPath + "/" + configObject.getWriterPath
    } else {
      throw new Exception("output scheme not recognized: " + configObject.getScheme)
    }
    return outPathBuilder
  }

  def toProviderId(input : String) : String = {
    if( input.startsWith("@") )
      return input.substring(1)
    if( input.contains(':'))
      return input.substring(input.lastIndexOf(':')+1)
    else return input
  }

}