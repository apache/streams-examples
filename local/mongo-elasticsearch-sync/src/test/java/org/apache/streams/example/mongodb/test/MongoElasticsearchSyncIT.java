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

package org.apache.streams.example.mongodb.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.elasticsearch.ElasticsearchClientManager;
import org.apache.streams.elasticsearch.example.MongoElasticsearchSync;
import org.apache.streams.example.elasticsearch.MongoElasticsearchSyncConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

/**
 * Test copying documents between two indexes on same cluster
 */
public class MongoElasticsearchSyncIT {

    private final static Logger LOGGER = LoggerFactory.getLogger(MongoElasticsearchSyncIT.class);

    ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    protected MongoElasticsearchSyncConfiguration testConfiguration;
    protected Client testClient;

    @Before
    public void prepareTest() throws Exception {

        Config reference  = ConfigFactory.load();
        File conf_file = new File("target/test-classes/MongoElasticsearchSyncIT.conf");
        assert(conf_file.exists());
        Config testResourceConfig  = ConfigFactory.parseFileAnySyntax(conf_file, ConfigParseOptions.defaults().setAllowMissing(false));
        Properties mongo_properties  = new Properties();
        InputStream mongo_stream  = new FileInputStream("mongo.properties");
        mongo_properties.load(mongo_stream);
        Config mongoProps  = ConfigFactory.parseProperties(mongo_properties);
        Properties es_properties  = new Properties();
        InputStream es_stream  = new FileInputStream("elasticsearch.properties");
        es_properties.load(es_stream);
        Config esProps  = ConfigFactory.parseProperties(es_properties);
        Config typesafe  = testResourceConfig.withFallback(mongoProps).withFallback(esProps).withFallback(reference).resolve();
        StreamsConfiguration streams  = StreamsConfigurator.detectConfiguration(typesafe);
        testConfiguration = new ComponentConfigurator<>(MongoElasticsearchSyncConfiguration.class).detectConfiguration(typesafe);
        testClient = new ElasticsearchClientManager(testConfiguration.getDestination()).getClient();

        ClusterHealthRequest clusterHealthRequest = Requests.clusterHealthRequest();
        ClusterHealthResponse clusterHealthResponse = testClient.admin().cluster().health(clusterHealthRequest).actionGet();
        assertNotEquals(clusterHealthResponse.getStatus(), ClusterHealthStatus.RED);

        IndicesExistsRequest indicesExistsRequest = Requests.indicesExistsRequest(testConfiguration.getDestination().getIndex());
        IndicesExistsResponse indicesExistsResponse = testClient.admin().indices().exists(indicesExistsRequest).actionGet();
        assertFalse(indicesExistsResponse.isExists());
    }

    @Test
    public void testSync() throws Exception {

        MongoElasticsearchSync sync = new MongoElasticsearchSync(testConfiguration);

        sync.run();

        IndicesExistsRequest indicesExistsRequest = Requests.indicesExistsRequest(testConfiguration.getDestination().getIndex());
        IndicesExistsResponse indicesExistsResponse = testClient.admin().indices().exists(indicesExistsRequest).actionGet();
        assertTrue(indicesExistsResponse.isExists());

        // assert lines in file
        SearchRequestBuilder countRequest = testClient
                .prepareSearch(testConfiguration.getDestination().getIndex())
                .setTypes(testConfiguration.getDestination().getType());
        SearchResponse countResponse = countRequest.execute().actionGet();

        assertEquals(89, (int)countResponse.getHits().getTotalHits());

    }
}
