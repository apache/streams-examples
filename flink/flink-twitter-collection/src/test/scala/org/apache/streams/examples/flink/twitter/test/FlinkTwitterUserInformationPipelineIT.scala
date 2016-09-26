package com.peoplepattern.streams.twitter.collection

import java.nio.file.{Files, Paths}

import com.peoplepattern.streams.pipelines.pdb.{TwitterPostsPipelineConfiguration, TwitterUserInformationPipelineConfiguration}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.streams.config.{ComponentConfigurator, StreamsConfigurator}
import org.apache.streams.hdfs.{HdfsConfiguration, HdfsReaderConfiguration, HdfsWriterConfiguration}
import org.scalatest.FlatSpec
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.slf4j.{Logger, LoggerFactory}

import scala.io.Source
import org.scalatest.Ignore
import org.scalatest.concurrent.Eventually._
import org.scalatest.time.{Seconds, Span}
import org.scalatest.time.SpanSugar._
import org.testng.annotations.Test

/**
  * Created by sblackmon on 3/13/16.
  */
class FlinkTwitterUserInformationPipelineIT extends FlatSpec {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FlinkTwitterUserInformationPipelineIT])

  @Test
  def flinkTwitterUserInformationPipelineIT = {

    val testConfig : TwitterUserInformationPipelineConfiguration =
      new ComponentConfigurator[TwitterUserInformationPipelineConfiguration](classOf[TwitterUserInformationPipelineConfiguration]).detectConfiguration(StreamsConfigurator.getConfig)
    val source : HdfsReaderConfiguration = new HdfsReaderConfiguration().withReaderPath("1000twitterids.txt").withScheme(HdfsConfiguration.Scheme.FILE).asInstanceOf[HdfsReaderConfiguration]
    source.setPath("target/test-classes")
    testConfig.setSource(source);
    val destination : HdfsWriterConfiguration = new HdfsWriterConfiguration().withWriterPath("pdb-twitter-collect/TwitterUserInformationPipeline").withScheme(HdfsConfiguration.Scheme.FILE).asInstanceOf[HdfsWriterConfiguration]
    destination.setPath("target/test-classes")
    testConfig.setDestination(destination)
    testConfig.setProviderWaitMs(1000l)
    testConfig.setTest(true)

    val job = new FlinkTwitterUserInformationPipeline(config = testConfig)
    val jobThread = new Thread(job)
    jobThread.start
    jobThread.join

    eventually (timeout(30 seconds), interval(1 seconds)) {
      assert(Files.exists(Paths.get("target/test-classes/TwitterUserInformationPipeline")))
      assert(
        Source.fromFile("target/test-classes/TwitterUserInformationPipeline", "UTF-8").getLines.size
          > 500)
    }

  }

}
