package org.apache.streams.examples.flink.twitter.test

import java.io.File
import java.nio.file.{Files, Paths}

import com.typesafe.config.{Config, ConfigFactory, ConfigParseOptions}
import org.apache.streams.config.{ComponentConfigurator, StreamsConfiguration, StreamsConfigurator}
import org.apache.streams.examples.flink.twitter.TwitterFollowingPipelineConfiguration
import org.apache.streams.examples.flink.twitter.collection.FlinkTwitterFollowingPipeline
import org.scalatest.FlatSpec
import org.scalatest.concurrent.Eventually._
import org.scalatest.time.SpanSugar._
import org.slf4j.{Logger, LoggerFactory}
import org.testng.annotations.Test

import scala.io.Source

/**
  * Created by sblackmon on 3/13/16.
  */
class FlinkTwitterFollowingPipelineFollowersIT extends FlatSpec {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FlinkTwitterFollowingPipelineFollowersIT])

  import FlinkTwitterFollowingPipeline._

  @Test
  def flinkTwitterFollowersPipelineFollowersIT = {

    val reference: Config = ConfigFactory.load()
    val conf_file: File = new File("target/test-classes/FlinkTwitterFollowingPipelineFollowersIT.conf")
    assert(conf_file.exists())
    val testResourceConfig: Config = ConfigFactory.parseFileAnySyntax(conf_file, ConfigParseOptions.defaults().setAllowMissing(false));

    val typesafe: Config = testResourceConfig.withFallback(reference).resolve()
    val streams: StreamsConfiguration = StreamsConfigurator.detectConfiguration(typesafe)
    val testConfig = new ComponentConfigurator(classOf[TwitterFollowingPipelineConfiguration]).detectConfiguration(typesafe)

    setup(testConfig)

    val job = new FlinkTwitterFollowingPipeline(config = testConfig)
    val jobThread = new Thread(job)
    jobThread.start
    jobThread.join

    eventually (timeout(60 seconds), interval(1 seconds)) {
      assert(Files.exists(Paths.get(testConfig.getDestination.getPath + "/" + testConfig.getDestination.getWriterPath)))
      assert(
        Source.fromFile(testConfig.getDestination.getPath + "/" + testConfig.getDestination.getWriterPath, "UTF-8").getLines.size
          > 4000)
    }

  }

}
