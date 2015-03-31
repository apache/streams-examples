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

[TwitterFollowGraph.dot](src/main/resources/TwitterFollowGraph.dot "TwitterFollowGraph.dot" )

Diagram:
-----------------

![TwitterFollowGraph.png](./TwitterFollowGraph.png?raw=true)

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

`mvn clean package verify`

Run:
--------

`java -cp target/twitter-follow-graph-0.2-incubating-SNAPSHOT.jar -Dconfig.file=src/main/resources/application.json org.apache.streams.example.graph.TwitterFollowGraph`

Deploy:
--------
`mvn -Pdocker clean package docker:build`

`docker tag twitter-follow-graph:0.2-incubating-SNAPSHOT <dockerregistry>:twitter-follow-graph:0.2-incubating-SNAPSHOT`

`docker push <dockerregistry>:twitter-follow-graph:0.2-incubating-SNAPSHOT`

`docker run <dockerregistry>:twitter-follow-graph:0.2-incubating-SNAPSHOT java -cp twitter-follow-graph-0.2-incubating-SNAPSHOT.jar -Dconfig.file=http://<location_of_config_file>.json org.apache.streams.example.graph.TwitterFollowGraph`

