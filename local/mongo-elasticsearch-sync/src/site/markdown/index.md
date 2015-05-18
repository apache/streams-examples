Apache Streams (incubating)
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

mongo-elasticsearch-sync
==============================

Requirements:
-------------
 - A running MongoDB 2.4+ instance
 - A running ElasticSearch 1.0.0+ instance

Description:
------------
Copies documents from mongodb to elasticsearch

Specification:
-----------------

[MongoElasticsearchSync.dot](MongoElasticsearchSync.dot "MongoElasticsearchSync.dot" )

Diagram:
-----------------

<a href="MongoElasticsearchSync.dot.svg" target="_self">MongoElasticsearchSync.dot.svg</a>

Configuration:
-----------------

[MongoElasticsearchSyncConfiguration.json](MongoElasticsearchSyncConfiguration.json "MongoElasticsearchSyncConfiguration.json" )

Example Configuration:
----------------------

    {
        "source": {
            "host": "localhost",
            "port": 27017,
            "db": "streams",
            "collection": "activities"
        },
        "destination": {
            "hosts": [
                "localhost"
            ],
            "port": 9300,
            "clusterName": "elasticsearch",
            "index": "destination",
            "type": "activity"
        }
    }

Build:
---------

`mvn clean package verify`

Deploy:
--------

    mvn -Pdocker clean package docker:build

Run:
--------

    java -cp dist/mongo-elasticsearch-sync-0.2-incubating-jar-with-dependencies.jar -Dconfig.file=`pwd`/src/test/resources/testSync.json org.apache.streams.example.elasticsearch.MongoElasticsearchSync

    docker run mongo-elasticsearch-sync:0.2-incubating java -cp /mongo-elasticsearch-sync-0.2-incubating-jar-with-dependencies.jar -Dconfig.url=http://<location_of_config_resource> org.apache.streams.example.elasticsearch.MongoElasticsearchSync

