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

package org.apache.streams.example.graph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.converter.ActivityConverterProcessor;
import org.apache.streams.converter.ActivityConverterProcessorConfiguration;
import org.apache.streams.converter.TypeConverterProcessor;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.data.DocumentClassifier;
import org.apache.streams.graph.GraphHttpConfiguration;
import org.apache.streams.graph.GraphHttpPersistWriter;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.apache.streams.twitter.TwitterUserInformationConfiguration;
import org.apache.streams.twitter.converter.TwitterFollowActivityConverter;
import org.apache.streams.twitter.pojo.Follow;
import org.apache.streams.twitter.provider.TwitterFollowingProvider;
import org.apache.streams.twitter.converter.TwitterDocumentClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Collects friend and follow connections for a set of twitter users and builds a graph
 * database in neo4j.
 */
public class TwitterFollowGraph {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterFollowGraph.class);

    public static void main(String[] args) {

        LOGGER.info(StreamsConfigurator.config.toString());

        StreamsConfiguration streams = StreamsConfigurator.detectConfiguration();

        TwitterFollowingGraphConfiguration configuration = new ComponentConfigurator<>(TwitterFollowingGraphConfiguration.class).detectConfiguration(StreamsConfigurator.getConfig());

        TwitterUserInformationConfiguration twitterUserInformationConfiguration = configuration.getTwitter();
        TwitterFollowingProvider followingProvider = new TwitterFollowingProvider(twitterUserInformationConfiguration);
        TypeConverterProcessor converter = new TypeConverterProcessor(String.class);

        ActivityConverterProcessorConfiguration activityConverterProcessorConfiguration =
                new ActivityConverterProcessorConfiguration()
                        .withClassifiers(Lists.newArrayList((DocumentClassifier) new TwitterDocumentClassifier()))
                        .withConverters(Lists.newArrayList((ActivityConverter) new TwitterFollowActivityConverter()));
        ActivityConverterProcessor activity = new ActivityConverterProcessor(activityConverterProcessorConfiguration);

        GraphHttpConfiguration graphWriterConfiguration = configuration.getGraph();
        GraphHttpPersistWriter graphPersistWriter = new GraphHttpPersistWriter(graphWriterConfiguration);

        StreamBuilder builder = new LocalStreamBuilder();
        builder.newPerpetualStream(TwitterFollowingProvider.STREAMS_ID, followingProvider);
        builder.addStreamsProcessor("converter", converter, 1, TwitterFollowingProvider.STREAMS_ID);
        builder.addStreamsProcessor("activity", activity, 1, "converter");
        builder.addStreamsPersistWriter("graph", graphPersistWriter, 1, "activity");

        builder.start();
    }

}