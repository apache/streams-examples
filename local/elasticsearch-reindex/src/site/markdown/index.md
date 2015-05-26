Apache Streams (incubating)
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

elasticsearch-reindex
==============================

Requirements:
-------------
 - A running ElasticSearch 1.0.0+ cluster
 - Transport client access to cluster
 - elasticsearch.version and lucene.version set to match cluster

Description:
------------
Copies documents into a different index

Specification:
-----------------

[ElasticsearchReindex.dot](ElasticsearchReindex.dot "ElasticsearchReindex.dot" )

Diagram:
-----------------

<a href="ElasticsearchReindex.dot.svg" target="_self">ElasticsearchReindex.dot.svg</a>

Configuration:
-----------------

[ElasticsearchReindexConfiguration.json](ElasticsearchReindexConfiguration.json "ElasticsearchReindexConfiguration.json" )

Example Configuration:
----------------------

    {
        "source": {
            "hosts": [
                "localhost"
            ],
            "port": 9300,
            "clusterName": "elasticsearch",
            "indexes": [
                "activity"
            ],
            "types": [
                "activity"
            ],
            "forceUseConfig": true
        },
        "destination": {
            "hosts": [
                "localhost"
            ],
            "port": 9300,
            "clusterName": "elasticsearch",
            "index": "activity2",
            "type": "activity",
            "forceUseConfig": true
        }
    }

Populate source and destination in configuration with cluster / index / type details.

Build:
---------

    mvn clean package verify

Deploy:
--------

    mvn -Pdocker clean package docker:build

Run:
--------

    java -cp dist/elasticsearch-reindex-0.2-incubating-jar-with-dependencies.jar -Dconfig.file=`pwd`/src/main/resources/application.json org.apache.streams.example.elasticsearch.ElasticsearchReindex
    
    docker run elasticsearch-reindex:0.2-incubating java -cp /elasticsearch-reindex-0.2-incubating-jar-with-dependencies.jar -Dconfig.url=http://<location_of_config_resource> org.apache.streams.example.elasticsearch.ElasticsearchReindex

