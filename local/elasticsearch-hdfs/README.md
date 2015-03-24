Apache Streams (incubating)
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

elasticsearch-hdfs
==============================

Requirements:
-------------
 - A running ElasticSearch 1.0.0+ instance

Description:
------------
Copies documents between elasticsearch and file system using the hdfs persist module.

Streams:
--------

[ElasticsearchHdfs](ElasticsearchHdfs.md "ElasticsearchHdfs" )

[HdfsElasticsearch](HdfsElasticsearch.md "HdfsElasticsearch" )

Build:
---------

`mvn clean package`

Note that an alternative version of hdfs is packaged, by excluding org.apache.hadoop.hadoop-hdfs when
importing org.apache.streams.streams-persist-hdfs, and specifically depending on a different preferred version.

Deploy:
--------

`mvn -Pdocker clean package docker:build`

