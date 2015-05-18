Apache Streams (incubating)
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

twitter-follow-graph
==============================

Requirements:
-------------
 - Authorized Twitter API credentials
 - A running Neo4J 1.9.0+ instance

Description:
------------
Collects friend or follower connections for a set of twitter users to build a graph database in neo4j.

Specification:
-----------------

[TwitterFollowGraph.dot](TwitterFollowGraph.dot "TwitterFollowGraph.dot" )

Diagram:
-----------------

<a href="TwitterFollowGraph.dot.svg" target="_self">TwitterFollowGraph.dot.svg</a>

Configuration:
-----------------

[TwitterFollowGraphConfiguration.json](TwitterFollowGraphConfiguration.json "TwitterFollowGraphConfiguration.json" )

Example Configuration:
----------------------

    {
      "twitter": {
        "endpoint": "friends",
        "oauth": {
          "consumerSecret": "",
          "consumerKey": "",
          "accessToken": "",
          "accessTokenSecret": ""
        },
        "info": [
          42232950
        ]
      },
      "graph": {
        "vertices": {
          "objects": [
            "actor",
            "object"
          ],
          "verbs": [
            "follow"
          ],
          "objectTypes": [
            "page"
          ]
        },
        "edges": {
          "objects": [
            "actor",
            "object"
          ],
          "verbs": [
            "follow"
          ],
          "objectTypes": [
            "page"
          ]
        },
        "protocol": "http",
        "hostname": "localhost",
        "port": 7474,
        "requestMethod": "GET",
        "content-type": "application/json",
        "type": "neo4j",
        "graph": "data"
      }
    }

Build:
---------

    mvn clean package verify

Deploy:
--------

    mvn -Pdocker clean package docker:build

Run:
--------

    java -cp dist/twitter-follow-graph-0.2-incubating-jar-with-dependencies.jar -Dconfig.file=`pwd`/src/main/resources/application.json org.apache.streams.example.graph.TwitterFollowGraph

    docker run twitter-follow-graph:0.2-incubating java -cp twitter-follow-graph-0.2-incubating-jar-with-dependencies.jar -Dconfig.file=`pwd`/src/main/resources/application.json org.apache.streams.example.graph.TwitterFollowGraph


