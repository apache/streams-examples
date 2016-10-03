hdfs-elasticsearch
==============================

Description:
-----------------

Copies documents from hdfs to elasticsearch.

Specification:
-----------------

[HdfsElasticsearch.dot](HdfsElasticsearch.dot "HdfsElasticsearch.dot" )

Diagram:
-----------------

![HdfsElasticsearch.dot.svg](./HdfsElasticsearch.dot.svg)

Example Configuration:
----------------------

[testRestore.json](testRestore.json "testRestore.json" )

Run (Local):
------------

    java -cp dist/elasticsearch-hdfs-jar-with-dependencies.jar -Dconfig.file=file://<location_of_config_file> org.apache.streams.elasticsearch.example.HdfsElasticsearch

Run (Docker):
-------------

    docker run elasticsearch-hdfs java -cp elasticsearch-hdfs-jar-with-dependencies.jar -Dconfig.url=http://<location_of_config_file> org.apache.streams.elasticsearch.example.HdfsElasticsearch

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0