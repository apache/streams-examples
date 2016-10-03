elasticsearch-hdfs
==============================

Description:
-----------------

Copies documents from elasticsearch to hdfs.

Specification:
-----------------

[ElasticsearchHdfs.dot](ElasticsearchHdfs.dot "ElasticsearchHdfs.dot" )

Diagram:
-----------------

![ElasticsearchHdfs.dot.svg](./ElasticsearchHdfs.dot.svg)

Example Configuration:
----------------------

[testBackup.json](testBackup.json "testBackup.json" )

Run (Local):
------------

    java -cp dist/elasticsearch-hdfs-jar-with-dependencies.jar -Dconfig.file=file://<location_of_config_file> org.apache.streams.elasticsearch.example.ElasticsearchHdfs

Run (Docker):
-------------

    docker run elasticsearch-hdfs java -cp elasticsearch-hdfs-jar-with-dependencies.jar -Dconfig.url=http://<location_of_config_file> org.apache.streams.elasticsearch.example.ElasticsearchHdfs

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0