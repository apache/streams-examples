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

package org.apache.streams.example.elasticsearch.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.elasticsearch.example.MongoElasticsearchSync;
import org.apache.streams.example.elasticsearch.MongoElasticsearchSyncConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.mongo.MongoPersistWriter;
import org.apache.streams.pojo.json.Activity;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

/**
 * Test copying documents between two indexes on same cluster
 */
@ElasticsearchIntegrationTest.ClusterScope(scope= ElasticsearchIntegrationTest.Scope.TEST, numNodes=1)
public class MongoElasticsearchSyncIT extends ElasticsearchIntegrationTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(MongoElasticsearchSyncIT.class);

    ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    MongoElasticsearchSyncConfiguration syncConfiguration;

    int srcCount = 0;

    @Before
    public void prepareTest() throws Exception {

        syncConfiguration = MAPPER.readValue(
                MongoElasticsearchSyncIT.class.getResourceAsStream("/testSync.json"), MongoElasticsearchSyncConfiguration.class);

        syncConfiguration.getDestination().setClusterName(cluster().getClusterName());

        MongoPersistWriter setupWriter = new MongoPersistWriter(syncConfiguration.getSource());

        setupWriter.prepare(null);

        InputStream testActivityFolderStream = MongoElasticsearchSyncIT.class.getClassLoader()
                .getResourceAsStream("activities");
        List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

        for( String file : files) {
            LOGGER.info("File: " + file );
            InputStream testActivityFileStream = MongoElasticsearchSyncIT.class.getClassLoader()
                    .getResourceAsStream("activities/" + file);
            Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
            activity.getAdditionalProperties().remove("$license");
            StreamsDatum datum = new StreamsDatum(activity, activity.getVerb());
            setupWriter.write( datum );
            LOGGER.info("Wrote: " + activity.getVerb() );
            srcCount++;
        }

        setupWriter.cleanUp();

    }

    @Test
    public void testSync() throws Exception {

        assert srcCount > 0;

        MongoElasticsearchSync sync = new MongoElasticsearchSync(syncConfiguration);

        Thread reindexThread = new Thread(sync);
        reindexThread.start();
        reindexThread.join();

        flushAndRefresh();

        assert(indexExists("destination"));

        long destCount = client().count(client().prepareCount("destination").request()).get().getCount();
        assert srcCount == destCount;

    }
}
