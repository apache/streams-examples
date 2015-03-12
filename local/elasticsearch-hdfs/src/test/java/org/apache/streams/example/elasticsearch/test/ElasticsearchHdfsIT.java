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
import com.google.common.collect.Lists;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.elasticsearch.ElasticsearchConfiguration;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.apache.streams.elasticsearch.ElasticsearchWriterConfiguration;
import org.apache.streams.elasticsearch.ElasticsearchPersistReader;
import org.apache.streams.elasticsearch.ElasticsearchReaderConfiguration;
import org.apache.streams.hdfs.HdfsConfiguration;
import org.apache.streams.hdfs.WebHdfsPersistWriter;
import org.apache.streams.hdfs.HdfsWriterConfiguration;
import org.apache.streams.hdfs.WebHdfsPersistReader;
import org.apache.streams.hdfs.HdfsReaderConfiguration;
import org.apache.streams.elasticsearch.example.ElasticsearchHdfs;
import org.apache.streams.elasticsearch.example.ElasticsearchHdfsConfiguration;
import org.apache.streams.elasticsearch.example.HdfsElasticsearch;
import org.apache.streams.elasticsearch.example.HdfsElasticsearchConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.File;
import java.util.*;

/**
 * Test copying documents between hdfs and elasticsearch
 */
@ElasticsearchIntegrationTest.ClusterScope(scope= ElasticsearchIntegrationTest.Scope.TEST, numNodes=1)
public class ElasticsearchHdfsIT extends ElasticsearchIntegrationTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchHdfsIT.class);

    ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    ElasticsearchConfiguration testConfiguration = new ElasticsearchConfiguration();

    @Before
    public void prepareTest() throws Exception {

        testConfiguration = new ElasticsearchConfiguration();
        testConfiguration.setHosts(Lists.newArrayList("localhost"));
        testConfiguration.setClusterName(cluster().getClusterName());

        ElasticsearchWriterConfiguration setupWriterConfiguration = MAPPER.convertValue(testConfiguration, ElasticsearchWriterConfiguration.class);
        setupWriterConfiguration.setIndex("source");
        setupWriterConfiguration.setType("activity");
        setupWriterConfiguration.setBatchSize(5l);

        ElasticsearchPersistWriter setupWriter = new ElasticsearchPersistWriter(setupWriterConfiguration);
        setupWriter.prepare(null);

        InputStream testActivityFolderStream = ElasticsearchHdfsIT.class.getClassLoader()
                .getResourceAsStream("activities");
        List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

        for( String file : files) {
            LOGGER.info("File: " + file );
            InputStream testActivityFileStream = ElasticsearchHdfsIT.class.getClassLoader()
                    .getResourceAsStream("activities/" + file);
            Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
            StreamsDatum datum = new StreamsDatum(activity, activity.getVerb());
            setupWriter.write( datum );
            LOGGER.info("Wrote: " + activity.getVerb() );
        }

        setupWriter.cleanUp();

        flushAndRefresh();

    }

    @Test
    public void testElasticsearchHdfs() throws Exception {

        ElasticsearchHdfsConfiguration backupConfiguration = MAPPER.readValue(
                ElasticsearchHdfsIT.class.getResourceAsStream("/testBackup.json"), ElasticsearchHdfsConfiguration.class);

        backupConfiguration.getSource().setClusterName(cluster().getClusterName());

        // backupConfiguration.getDestination().setClusterName(cluster().getClusterName());

        assert(indexExists("source"));
        long srcCount = client().count(client().prepareCount("source").request()).get().getCount();
        assert srcCount > 0;

        ElasticsearchHdfs backup = new ElasticsearchHdfs(backupConfiguration);

        Thread backupThread = new Thread(backup);
        backupThread.start();
        backupThread.join();

        HdfsElasticsearchConfiguration restoreConfiguration = MAPPER.readValue(
                ElasticsearchHdfsIT.class.getResourceAsStream("/testRestore.json"), HdfsElasticsearchConfiguration.class);

        restoreConfiguration.getDestination().setClusterName(cluster().getClusterName());

        assert(!indexExists("destination"));

        HdfsElasticsearch restore = new HdfsElasticsearch(restoreConfiguration);

        Thread restoreThread = new Thread(restore);
        restoreThread.start();
        restoreThread.join();

        flushAndRefresh();

        long destCount = client().count(client().prepareCount("destination").request()).get().getCount();

        assert srcCount == destCount;
    }

}
