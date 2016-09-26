package com.peoplepattern.streams.twitter.collection

import java.nio.file.{Files, Paths}

import com.peoplepattern.streams.pipelines.pdb.{TwitterFollowingPipelineConfiguration, TwitterPostsPipelineConfiguration}
import org.apache.streams.config.{ComponentConfigurator, StreamsConfigurator}
import org.apache.streams.hdfs.{HdfsConfiguration, HdfsReaderConfiguration, HdfsWriterConfiguration}
import org.scalatest.FlatSpec
import org.scalatest.concurrent.Eventually._
import org.scalatest.time.SpanSugar._
import org.slf4j.{Logger, LoggerFactory}
import org.testng.annotations.Test

import scala.io.Source

/**
  * Created by sblackmon on 3/13/16.
  */
class FlinkTwitterFollowingPipelineIT extends FlatSpec {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FlinkTwitterFollowingPipelineIT])

  @Test
  def flinkTwitterFollowersPipelineFriendsIT = {

    val testConfig : TwitterFollowingPipelineConfiguration =
      new ComponentConfigurator[TwitterFollowingPipelineConfiguration](classOf[TwitterFollowingPipelineConfiguration]).detectConfiguration(StreamsConfigurator.getConfig)
    testConfig.getTwitter.setEndpoint("friends")
    val source : HdfsReaderConfiguration = new HdfsReaderConfiguration().withReaderPath("asf.txt").withScheme(HdfsConfiguration.Scheme.FILE).asInstanceOf[HdfsReaderConfiguration]
    source.setPath("target/test-classes")
    testConfig.setSource(source);
    val destination : HdfsWriterConfiguration = new HdfsWriterConfiguration().withWriterPath("pdb-twitter-collect/FlinkTwitterFollowingPipeline/friends").withScheme(HdfsConfiguration.Scheme.FILE).asInstanceOf[HdfsWriterConfiguration]
    destination.setPath("target/test-classes")
    testConfig.setDestination(destination)
    testConfig.setProviderWaitMs(1000l)
    testConfig.setTest(true)

    val job = new FlinkTwitterFollowingPipeline(config = testConfig)
    val jobThread = new Thread(job)
    jobThread.start
    jobThread.join

    eventually (timeout(30 seconds), interval(1 seconds)) {
      assert(Files.exists(Paths.get("target/test-classes/pdb-twitter-collect/FlinkTwitterFollowingPipeline/friends")))
      assert(
        Source.fromFile("target/test-classes/pdb-twitter-collect/FlinkTwitterFollowingPipeline/friends", "UTF-8").getLines.size
          > 90)
    }

  }

  @Test
  def flinkTwitterFollowersPipelineFollowersIT = {

    val testConfig : TwitterFollowingPipelineConfiguration =
      new ComponentConfigurator[TwitterFollowingPipelineConfiguration](classOf[TwitterFollowingPipelineConfiguration]).detectConfiguration(StreamsConfigurator.getConfig)
    testConfig.getTwitter.setEndpoint("followers")
    val source : HdfsReaderConfiguration = new HdfsReaderConfiguration().withReaderPath("asf.txt").withScheme(HdfsConfiguration.Scheme.FILE).asInstanceOf[HdfsReaderConfiguration]
    source.setPath("target/test-classes")
    testConfig.setSource(source);
    val destination : HdfsWriterConfiguration = new HdfsWriterConfiguration().withWriterPath("pdb-twitter-collect/FlinkTwitterFollowingPipeline/followers").withScheme(HdfsConfiguration.Scheme.FILE).asInstanceOf[HdfsWriterConfiguration]
    destination.setPath("target/test-classes")
    testConfig.setDestination(destination)
    testConfig.setProviderWaitMs(1000l)
    testConfig.setTest(true)

    val job = new FlinkTwitterFollowingPipeline(config = testConfig)
    val jobThread = new Thread(job)
    jobThread.start
    jobThread.join

    eventually (timeout(30 seconds), interval(1 seconds)) {
      assert(Files.exists(Paths.get("target/test-classes/FlinkTwitterFollowingPipeline/followers")))
      assert(
        Source.fromFile("target/test-classes/FlinkTwitterFollowingPipeline/followers", "UTF-8").getLines.size
          > 500)
    }

  }

}
