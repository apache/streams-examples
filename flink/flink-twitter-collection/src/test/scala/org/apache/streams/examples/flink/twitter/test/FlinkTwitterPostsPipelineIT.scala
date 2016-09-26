package com.peoplepattern.streams.twitter.collection

import java.nio.file.{Files, Paths}
import java.util.concurrent.TimeUnit

import com.google.common.util.concurrent.{Monitor, Uninterruptibles}
import com.peoplepattern.streams.pipelines.pdb.TwitterPostsPipelineConfiguration
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.streams.config.{ComponentConfigurator, StreamsConfigurator}
import org.apache.streams.hdfs.{HdfsConfiguration, HdfsReaderConfiguration, HdfsWriterConfiguration}
import org.slf4j.{Logger, LoggerFactory}

import scala.io.Source
import org.scalatest.FlatSpec
import org.scalatest.concurrent.Eventually._
import org.scalatest.time.{Seconds, Span}
import org.scalatest.time.SpanSugar._
import org.testng.annotations.Test

/**
  * Created by sblackmon on 3/13/16.
  */
class FlinkTwitterPostsPipelineIT extends FlatSpec {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FlinkTwitterPostsPipelineIT])

  @Test
  def flinkTwitterPostsPipelineIT = {

    val testConfig : TwitterPostsPipelineConfiguration =
      new ComponentConfigurator[TwitterPostsPipelineConfiguration](classOf[TwitterPostsPipelineConfiguration]).detectConfiguration(StreamsConfigurator.getConfig)
    val source : HdfsReaderConfiguration = new HdfsReaderConfiguration().withReaderPath("asf.txt").withScheme(HdfsConfiguration.Scheme.FILE).asInstanceOf[HdfsReaderConfiguration]
    source.setPath("target/test-classes")
    testConfig.setSource(source);
    val destination : HdfsWriterConfiguration = new HdfsWriterConfiguration().withWriterPath("pdb-twitter-collect/FlinkTwitterPostsPipeline").withScheme(HdfsConfiguration.Scheme.FILE).asInstanceOf[HdfsWriterConfiguration]
    destination.setPath("target/test-classes")
    testConfig.setDestination(destination)
    testConfig.setProviderWaitMs(1000l)
    testConfig.setTest(true)

    val job = new FlinkTwitterPostsPipeline(config = testConfig)
    val jobThread = new Thread(job)
    jobThread.start
    jobThread.join

    eventually (timeout(30 seconds), interval(1 seconds)) {
      assert(Files.exists(Paths.get("target/test-classes/FlinkTwitterPostsPipeline")))
      assert(
        Source.fromFile("target/test-classes/FlinkTwitterPostsPipeline", "UTF-8").getLines.size
          >= 200)
    }

  }

}
