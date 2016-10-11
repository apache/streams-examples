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
import org.apache.streams.elasticsearch.ElasticsearchPersistReader;
import org.apache.streams.example.ElasticsearchHdfsConfiguration;
import org.apache.streams.hdfs.WebHdfsPersistWriter;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Copies documents into a new index
 */
public class ElasticsearchHdfs implements Runnable {

    public final static String STREAMS_ID = "ElasticsearchHdfs";

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchHdfs.class);

    ElasticsearchHdfsConfiguration config;

    public ElasticsearchHdfs() {
       this(new ComponentConfigurator<>(ElasticsearchHdfsConfiguration.class).detectConfiguration(StreamsConfigurator.getConfig()));

    }

    public ElasticsearchHdfs(ElasticsearchHdfsConfiguration reindex) {
        this.config = reindex;
    }

    public static void main(String[] args)
    {
        LOGGER.info(StreamsConfigurator.config.toString());

        ElasticsearchHdfs backup = new ElasticsearchHdfs();

        new Thread(backup).start();

    }

    @Override
    public void run() {

        ElasticsearchPersistReader elasticsearchPersistReader = new ElasticsearchPersistReader(config.getSource());

        WebHdfsPersistWriter hdfsPersistWriter = new WebHdfsPersistWriter(config.getDestination());

        Map<String, Object> streamConfig = Maps.newHashMap();
        streamConfig.put(LocalStreamBuilder.STREAM_IDENTIFIER_KEY, STREAMS_ID);
        streamConfig.put(LocalStreamBuilder.TIMEOUT_KEY, 7 * 24 * 60 * 1000);
        StreamBuilder builder = new LocalStreamBuilder(1000, streamConfig);

        builder.newPerpetualStream(ElasticsearchPersistReader.STREAMS_ID, elasticsearchPersistReader);
        builder.addStreamsPersistWriter(WebHdfsPersistWriter.STREAMS_ID, hdfsPersistWriter, 1, ElasticsearchPersistReader.STREAMS_ID);
        builder.start();
    }
}
