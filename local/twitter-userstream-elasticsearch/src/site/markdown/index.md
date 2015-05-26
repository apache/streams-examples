Apache Streams (incubating)
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

twitter-userstream-elasticsearch
==============================

Requirements:
-------------
 - Authorized Twitter API credentials
 - A running ElasticSearch 1.0.0+ instance

Description:
------------
This example connects to an active twitter account and stores the userstream as activities in Elasticsearch

Specification:
-----------------

[TwitterUserstreamElasticsearch.dot](TwitterUserstreamElasticsearch.dot "TwitterUserstreamElasticsearch.dot" )

Diagram:
-----------------

<a href="TwitterUserstreamElasticsearch.dot.svg" target="_self">TwitterUserstreamElasticsearch.dot.svg</a>

Configuration:
-----------------

[TwitterUserstreamElasticsearchConfiguration.json](TwitterUserstreamElasticsearchConfiguration.json "TwitterUserstreamElasticsearchConfiguration.json" )


Example Configuration:
----------------------

    twitter {
        endpoint = "userstream"
        oauth {
                consumerKey = "bcg14JThZEGoZ3MZOoT2HnJS7"
                consumerSecret = "S4dwxnZni58CIJaoupGnUrO4HRHmbBGOb28W6IqOJBx36LPw2z"
                accessToken = ""
                accessTokenSecret = ""
        }
    }
    elasticsearch {
        hosts = [
            localhost
        ]
        port = 9300
        clusterName = elasticsearch
        index = userstream_activity
        type = activity
        batchSize = 1
    }

In the Twitter section you should place all of your relevant authentication keys.

Build:
---------

    mvn clean package verify

Deploy:
--------

    mvn -Pdocker clean package docker:build

Run:
--------

    java -cp dist/twitter-userstream-elasticsearch-0.2-incubating-jar-with-dependencies.jar -Dconfig.file=`pwd`/src/main/resources/application.json org.apache.streams.example.twitter.TwitterUserstreamElasticsearch`

    docker run twitter-userstream-elasticsearch:0.2-incubating java -cp /twitter-userstream-elasticsearch-0.2-incubating-jar-with-dependencies.jar -Dconfig.url=http://<location_of_config_resource> org.apache.streams.example.twitter.TwitterUserstreamElasticsearch`
