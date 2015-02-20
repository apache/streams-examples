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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.data.util.ActivityUtil;
import org.apache.streams.elasticsearch.ElasticsearchConfigurator;
import org.apache.streams.elasticsearch.ElasticsearchPersistDeleter;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.apache.streams.elasticsearch.ElasticsearchWriterConfiguration;
import org.apache.streams.example.twitter.TwitterUserstreamElasticsearchConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Delete;
import org.apache.streams.pojo.json.Follow;
import org.apache.streams.pojo.json.Page;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.twitter.pojo.FriendList;
import org.apache.streams.twitter.pojo.UserstreamEvent;
import org.apache.streams.twitter.processor.TwitterTypeConverter;
import org.apache.streams.twitter.provider.TwitterConfigurator;
import org.apache.streams.twitter.provider.TwitterStreamProvider;
import org.apache.streams.twitter.serializer.StreamsTwitterMapper;
import org.apache.streams.twitter.serializer.util.TwitterActivityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Example stream that populates elasticsearch with activities from twitter userstream in real-time
 */
public class TwitterUserstreamElasticsearch implements Runnable {

    public final static String STREAMS_ID = "TwitterUserstreamElasticsearch";

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterUserstreamElasticsearch.class);

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
        TwitterTypeConverter converter = new TwitterTypeConverter(ObjectNode.class, Activity.class);
        ElasticsearchPersistWriter writer = new ElasticsearchPersistWriter(elasticsearchWriterConfiguration);
        DeleteOnlyProcessor deleteOnlyProcessor = new DeleteOnlyProcessor();
        NoDeletesProcessor noDeletesProcessor = new NoDeletesProcessor();
        ElasticsearchPersistDeleter deleter = new ElasticsearchPersistDeleter(elasticsearchWriterConfiguration);

        Map<String, Object> streamConfig = Maps.newHashMap();
        streamConfig.put(LocalStreamBuilder.TIMEOUT_KEY, 12 * 60 * 1000);
        StreamBuilder builder = new LocalStreamBuilder(25);

        builder.newPerpetualStream(TwitterStreamProvider.STREAMS_ID, stream);
        builder.addStreamsProcessor("converter", converter, 2, TwitterStreamProvider.STREAMS_ID);
        builder.addStreamsProcessor("NoDeletesProcessor", noDeletesProcessor, 1, "converter");
        builder.addStreamsPersistWriter(ElasticsearchPersistWriter.STREAMS_ID, writer, 1, "NoDeletesProcessor");
        builder.addStreamsProcessor("DeleteOnlyProcessor", deleteOnlyProcessor, 1, "converter");
        builder.addStreamsPersistWriter(ElasticsearchPersistDeleter.STREAMS_ID, deleter, 1, "DeleteOnlyProcessor");

        builder.start();

    }

    private class DeleteOnlyProcessor implements StreamsProcessor
    {
        String delete = new Delete().getVerb();

        @Override
        public void prepare(Object configurationObject) {}

        @Override
        public void cleanUp() {}

        @Override
        public List<StreamsDatum> process(StreamsDatum entry) {
            Preconditions.checkArgument(entry.getDocument() instanceof Activity);
            Activity activity = (Activity) entry.getDocument();
            if( activity.getVerb().equals(delete))
                return Lists.newArrayList(entry);
            else
                return Lists.newArrayList();
        }
    }

    private class NoDeletesProcessor implements StreamsProcessor
    {
        String delete = new Delete().getVerb();

        @Override
        public void prepare(Object configurationObject) {}

        @Override
        public void cleanUp() {}

        @Override
        public List<StreamsDatum> process(StreamsDatum entry) {
            Preconditions.checkArgument(entry.getDocument() instanceof Activity);
            Activity activity = (Activity) entry.getDocument();
            if( activity.getVerb().equals(delete))
                return Lists.newArrayList();
            else
                return Lists.newArrayList(entry);
        }
    }

}
