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

import com.google.common.collect.Maps;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.example.HdfsElasticsearchConfiguration;
import org.apache.streams.hdfs.WebHdfsPersistReader;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Copies documents into a new index
 */
public class HdfsElasticsearch implements Runnable {

    public final static String STREAMS_ID = "HdfsElasticsearch";

    private final static Logger LOGGER = LoggerFactory.getLogger(HdfsElasticsearch.class);

    HdfsElasticsearchConfiguration config;

    public HdfsElasticsearch() {
       this(new ComponentConfigurator<>(HdfsElasticsearchConfiguration.class).detectConfiguration(StreamsConfigurator.getConfig()));

    }

    public HdfsElasticsearch(HdfsElasticsearchConfiguration reindex) {
        this.config = reindex;
    }

    public static void main(String[] args)
    {
        LOGGER.info(StreamsConfigurator.config.toString());

        HdfsElasticsearch restore = new HdfsElasticsearch();

        new Thread(restore).start();

    }

    @Override
    public void run() {

        WebHdfsPersistReader webHdfsPersistReader = new WebHdfsPersistReader(config.getSource());

        ElasticsearchPersistWriter elasticsearchPersistWriter = new ElasticsearchPersistWriter(config.getDestination());

        Map<String, Object> streamConfig = Maps.newHashMap();
        streamConfig.put(LocalStreamBuilder.STREAM_IDENTIFIER_KEY, STREAMS_ID);
        streamConfig.put(LocalStreamBuilder.TIMEOUT_KEY, 1000 * 1000);
        StreamBuilder builder = new LocalStreamBuilder(1000, streamConfig);

        builder.newPerpetualStream(WebHdfsPersistReader.STREAMS_ID, webHdfsPersistReader);
        builder.addStreamsPersistWriter(ElasticsearchPersistWriter.STREAMS_ID, elasticsearchPersistWriter, 1, WebHdfsPersistReader.STREAMS_ID);
        builder.start();
    }
}
