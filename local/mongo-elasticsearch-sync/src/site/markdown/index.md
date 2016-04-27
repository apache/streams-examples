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

![MongoElasticsearchSync.dot.svg](./MongoElasticsearchSync.dot.svg)

Example Configuration:
----------------------

[testSync.json](testSync.json "testSync.json" )

Build:
---------

    mvn clean package verify

Run (Local):
------------

    java -cp dist/mongo-elasticsearch-sync-jar-with-dependencies.jar -Dconfig.file=file://<location_of_config_file> org.apache.streams.example.elasticsearch.MongoElasticsearchSync

Deploy (Docker):
----------------

    mvn -Pdocker -Ddocker.repo=<your docker host>:<your docker repo> docker:build docker:push

Run (Docker):
-------------

    docker run mongo-elasticsearch-sync java -cp mongo-elasticsearch-sync-jar-with-dependencies.jar -Dconfig.url=http://<location_of_config_file> org.apache.streams.elasticsearch.example.MongoElasticsearchSync

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
