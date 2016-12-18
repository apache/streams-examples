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

package org.apache.streams.example.test;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.example.TwitterFollowNeo4j;
import org.apache.streams.example.TwitterFollowNeo4jConfiguration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * TwitterFollowNeo4jIT is an integration test for TwitterFollowNeo4j.
 */
public class TwitterFollowNeo4jIT {

  private final static Logger LOGGER = LoggerFactory.getLogger(TwitterFollowNeo4jIT.class);

  protected TwitterFollowNeo4jConfiguration testConfiguration;

  private int count = 0;

  @BeforeClass
  public void prepareTest() throws Exception {

    Config reference  = ConfigFactory.load();
    File conf_file = new File("target/test-classes/TwitterFollowNeo4jIT.conf");
    assert(conf_file.exists());
    Config testResourceConfig  = ConfigFactory.parseFileAnySyntax(conf_file, ConfigParseOptions.defaults().setAllowMissing(false));
    Config typesafe  = testResourceConfig.withFallback(reference).resolve();
    testConfiguration = new ComponentConfigurator<>(TwitterFollowNeo4jConfiguration.class).detectConfiguration(typesafe);

  }

  @Test
  public void testTwitterFollowGraph() throws Exception {

    TwitterFollowNeo4j stream = new TwitterFollowNeo4j(testConfiguration);

    stream.run();

  }

}
