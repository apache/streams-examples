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

![ElasticsearchReindex.dot.svg](./ElasticsearchReindex.dot.svg)

Example Configuration:
----------------------

[testReindex.json](testReindex.json "testReindex.json" )

Populate source and destination in configuration with cluster / index / type details.

Build:
---------

    mvn clean package verify

Run (Local):
------------

    java -cp dist/elasticsearch-reindex-jar-with-dependencies.jar -Dconfig.file=file://<location_of_config_file> org.apache.streams.example.elasticsearch.ElasticsearchReindex

Deploy (Docker):
----------------

    mvn -Pdocker -Ddocker.repo=<your docker host>:<your docker repo> docker:build docker:push

Run (Docker):
-------------

    docker run elasticsearch-reindex java -cp elasticsearch-reindex-jar-with-dependencies.jar -Dconfig.url=http://<location_of_config_file> org.apache.streams.elasticsearch.example.ElasticsearchReindex

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0