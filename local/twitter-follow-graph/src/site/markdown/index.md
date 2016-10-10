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

![TwitterFollowGraph.dot.svg](./TwitterFollowGraph.dot.svg)

Example Configuration:
----------------------

[testGraph.json](testGraph.json "testGraph.json" )

Build:
---------

    mvn clean package verify

Test:
-----
Create a local file `application.conf` with valid twitter credentials

    twitter {
      oauth {
        consumerKey = ""
        consumerSecret = ""
        accessToken = ""
        accessTokenSecret = ""
      }
    }

Start up neo4j with docker:

    mvn -PdockerITs docker:start
    
Build with integration testing enabled, using your credentials

    mvn clean test verify -DskipITs=false -DargLine="-Dconfig.file=`pwd`/application.conf"

Shutdown neo4j when finished:

    mvn -PdockerITs docker:stop

Run (Local):
------------

    java -cp dist/twitter-follow-graph-jar-with-dependencies.jar -Dconfig.file=file://<location_of_config_file> org.apache.streams.example.graph.TwitterFollowGraph

Deploy (Docker):
----------------

    mvn -Pdocker -Ddocker.repo=<your docker host>:<your docker repo> docker:build docker:push

Run (Docker):
-------------

    docker run twitter-follow-graph java -cp twitter-follow-graph-jar-with-dependencies.jar -Dconfig.url=http://<location_of_config_file> org.apache.streams.elasticsearch.example.TwitterFollowGraph

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
