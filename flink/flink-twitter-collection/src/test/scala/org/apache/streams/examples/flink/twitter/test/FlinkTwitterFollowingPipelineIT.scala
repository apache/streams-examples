package com.peoplepattern.streams.twitter.collection

import java.io.File
import java.nio.file.{Files, Paths}

import org.apache.streams.examples.flink.twitter.TwitterFollowingPipelineConfiguration
import com.typesafe.config.{Config, ConfigFactory, ConfigParseOptions}
import org.apache.streams.config.{ComponentConfigurator, StreamsConfiguration, StreamsConfigurator}
import org.apache.streams.examples.flink.twitter.collection.FlinkTwitterUserInformationPipeline._
import org.apache.streams.examples.flink.twitter.collection.{FlinkTwitterFollowingPipeline, FlinkTwitterSpritzerPipeline}
import org.apache.streams.hdfs.{HdfsConfiguration, HdfsReaderConfiguration, HdfsWriterConfiguration}
import org.slf4j.{Logger, LoggerFactory}
import org.testng.annotations.Test

import scala.io.Source
import org.scalatest.FlatSpec
import org.scalatest.concurrent.Eventually._
import org.scalatest.time.{Seconds, Span}
import org.scalatest.time.SpanSugar._

/**
  * Created by sblackmon on 3/13/16.
  */
class FlinkTwitterFollowingPipelineIT extends FlatSpec {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FlinkTwitterFollowingPipelineIT])

  import FlinkTwitterFollowingPipeline._

  @Test
  def flinkTwitterFollowersPipelineFriendsIT = {

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
          > 90)
    }

  }

  @Test
  def flinkTwitterFollowersPipelineFollowersIT = {

    val reference: Config = ConfigFactory.load()
    val conf_file: File = new File("target/test-classes/FlinkTwitterFollowingPipelineFriendsIT.conf")
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
          > 500)
    }

  }

}
