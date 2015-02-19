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

package org.apache.streams.example.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.typesafe.config.Config;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.elasticsearch.*;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.apache.streams.mongo.MongoPersistReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Copies documents into a new index
 */
public class MongoElasticsearchSync implements Runnable {

    public final static String STREAMS_ID = "MongoElasticsearchSync";

    private final static Logger LOGGER = LoggerFactory.getLogger(MongoElasticsearchSync.class);

    protected static ListeningExecutorService executor = MoreExecutors.listeningDecorator(newFixedThreadPoolWithQueueSize(5, 20));

    private static ExecutorService newFixedThreadPoolWithQueueSize(int nThreads, int queueSize) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                5000L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    MongoElasticsearchSyncConfiguration config;

    public MongoElasticsearchSync() {
        this(new ComponentConfigurator<MongoElasticsearchSyncConfiguration>(MongoElasticsearchSyncConfiguration.class).detectConfiguration(StreamsConfigurator.getConfig()));

    }

    public MongoElasticsearchSync(MongoElasticsearchSyncConfiguration config) {
        this.config = config;
    }

    public static void main(String[] args)
    {
        LOGGER.info(StreamsConfigurator.config.toString());

        executor.submit(new MongoElasticsearchSync());

    }

    @Override
    public void run() {

        MongoPersistReader mongoPersistReader = new MongoPersistReader(config.getSource());

        ElasticsearchPersistWriter elasticsearchPersistWriter = new ElasticsearchPersistWriter(config.getDestination());

        Map<String, Object> streamConfig = Maps.newHashMap();
        streamConfig.put(LocalStreamBuilder.TIMEOUT_KEY, 7 * 24 * 60 * 1000);
        StreamBuilder builder = new LocalStreamBuilder(1000, streamConfig);

        builder.newPerpetualStream(MongoPersistReader.STREAMS_ID, mongoPersistReader);
        builder.addStreamsPersistWriter(ElasticsearchPersistWriter.STREAMS_ID, elasticsearchPersistWriter, 1, MongoPersistReader.STREAMS_ID);
        builder.start();
    }
}
