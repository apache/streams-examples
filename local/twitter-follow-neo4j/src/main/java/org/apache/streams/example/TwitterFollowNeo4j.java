/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.streams.example;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.converter.ActivityConverterProcessor;
import org.apache.streams.converter.ActivityConverterProcessorConfiguration;
import org.apache.streams.converter.TypeConverterProcessor;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.data.DocumentClassifier;
import org.apache.streams.graph.GraphHttpConfiguration;
import org.apache.streams.graph.GraphHttpPersistWriter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.local.LocalRuntimeConfiguration;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.apache.streams.twitter.TwitterFollowingConfiguration;
import org.apache.streams.twitter.converter.TwitterDocumentClassifier;
import org.apache.streams.twitter.converter.TwitterFollowActivityConverter;
import org.apache.streams.twitter.provider.TwitterFollowingProvider;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects friend and follow connections for a set of twitter users and builds a graph
 * database in neo4j.
 */
public class TwitterFollowNeo4j implements Runnable {

  private final static Logger LOGGER = LoggerFactory.getLogger(TwitterFollowNeo4j.class);

  TwitterFollowNeo4jConfiguration config;

  public TwitterFollowNeo4j() {
    this(new ComponentConfigurator<>(TwitterFollowNeo4jConfiguration.class).detectConfiguration(StreamsConfigurator.getConfig()));
  }

  public TwitterFollowNeo4j(TwitterFollowNeo4jConfiguration config) {
    this.config = config;
  }

  public void run() {

    TwitterFollowingConfiguration twitterFollowingConfiguration = config.getTwitter();
    TwitterFollowingProvider followingProvider = new TwitterFollowingProvider(twitterFollowingConfiguration);
    TypeConverterProcessor converter = new TypeConverterProcessor(String.class);

    ActivityConverterProcessorConfiguration activityConverterProcessorConfiguration =
        new ActivityConverterProcessorConfiguration()
            .withClassifiers(Lists.newArrayList((DocumentClassifier) new TwitterDocumentClassifier()))
            .withConverters(Lists.newArrayList((ActivityConverter) new TwitterFollowActivityConverter()));
    ActivityConverterProcessor activity = new ActivityConverterProcessor(activityConverterProcessorConfiguration);

    GraphHttpConfiguration graphWriterConfiguration = config.getGraph();
    GraphHttpPersistWriter graphPersistWriter = new GraphHttpPersistWriter(graphWriterConfiguration);

    LocalRuntimeConfiguration localRuntimeConfiguration =
        StreamsJacksonMapper.getInstance().convertValue(StreamsConfigurator.detectConfiguration(), LocalRuntimeConfiguration.class);
    StreamBuilder builder = new LocalStreamBuilder(localRuntimeConfiguration);

    builder.newPerpetualStream(TwitterFollowingProvider.class.getCanonicalName(), followingProvider);
    builder.addStreamsProcessor(TypeConverterProcessor.class.getCanonicalName(), converter, 1, TwitterFollowingProvider.class.getCanonicalName());
    builder.addStreamsProcessor(ActivityConverterProcessor.class.getCanonicalName(), activity, 1, TypeConverterProcessor.class.getCanonicalName());
    builder.addStreamsPersistWriter(GraphHttpPersistWriter.class.getCanonicalName(), graphPersistWriter, 1, ActivityConverterProcessor.class.getCanonicalName());

    builder.start();
  }

  public static void main(String[] args) {

    LOGGER.info(StreamsConfigurator.config.toString());

    TwitterFollowNeo4j stream = new TwitterFollowNeo4j();

    stream.run();

  }

}