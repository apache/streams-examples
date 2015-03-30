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

package org.apache.streams.twitter.example;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.converter.ActivityConverterProcessor;
import org.apache.streams.elasticsearch.ElasticsearchPersistDeleter;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.apache.streams.elasticsearch.ElasticsearchWriterConfiguration;
import org.apache.streams.example.twitter.TwitterUserstreamElasticsearchConfiguration;
import org.apache.streams.filters.VerbDefinitionDropFilter;
import org.apache.streams.filters.VerbDefinitionKeepFilter;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.twitter.provider.TwitterStreamProvider;
import org.apache.streams.verbs.VerbDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Example stream that populates elasticsearch with activities from twitter userstream in real-time
 */
public class TwitterUserstreamElasticsearch implements Runnable {

    public final static String STREAMS_ID = "TwitterUserstreamElasticsearch";

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterUserstreamElasticsearch.class);

    private static VerbDefinition deleteVerbDefinition = new VerbDefinition().withValue("post");

    TwitterUserstreamElasticsearchConfiguration config;

    public TwitterUserstreamElasticsearch() {
        this(new ComponentConfigurator<>(TwitterUserstreamElasticsearchConfiguration.class).detectConfiguration(StreamsConfigurator.getConfig()));

    }

    public TwitterUserstreamElasticsearch(TwitterUserstreamElasticsearchConfiguration config) {
        this.config = config;
    }

    public static void main(String[] args)
    {
        LOGGER.info(StreamsConfigurator.config.toString());

        TwitterUserstreamElasticsearch userstream = new TwitterUserstreamElasticsearch();
        new Thread(userstream).start();

    }

    @Override
    public void run() {

        TwitterStreamConfiguration twitterStreamConfiguration = config.getTwitter();
        ElasticsearchWriterConfiguration elasticsearchWriterConfiguration = config.getElasticsearch();

        TwitterStreamProvider stream = new TwitterStreamProvider(twitterStreamConfiguration);
        ActivityConverterProcessor converter = new ActivityConverterProcessor();
        VerbDefinitionDropFilter noDeletesProcessor = new VerbDefinitionDropFilter(Sets.newHashSet(deleteVerbDefinition));
        ElasticsearchPersistWriter writer = new ElasticsearchPersistWriter(elasticsearchWriterConfiguration);
        VerbDefinitionKeepFilter deleteOnlyProcessor = new VerbDefinitionKeepFilter(Sets.newHashSet(deleteVerbDefinition));
        ElasticsearchPersistDeleter deleter = new ElasticsearchPersistDeleter(elasticsearchWriterConfiguration);

        Map<String, Object> streamConfig = Maps.newHashMap();
        streamConfig.put(LocalStreamBuilder.TIMEOUT_KEY, 12 * 60 * 1000);
        StreamBuilder builder = new LocalStreamBuilder(25, streamConfig);

        builder.newPerpetualStream(TwitterStreamProvider.STREAMS_ID, stream);
        builder.addStreamsProcessor("converter", converter, 2, TwitterStreamProvider.STREAMS_ID);
        builder.addStreamsProcessor("NoDeletesProcessor", noDeletesProcessor, 1, "converter");
        builder.addStreamsPersistWriter(ElasticsearchPersistWriter.STREAMS_ID, writer, 1, "NoDeletesProcessor");
        builder.addStreamsProcessor("DeleteOnlyProcessor", deleteOnlyProcessor, 1, "converter");
        builder.addStreamsPersistWriter("deleter", deleter, 1, "DeleteOnlyProcessor");

        builder.start();

    }

}
