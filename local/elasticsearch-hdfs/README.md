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
Copies documents between elasticsearch and file system.

Streams:
--------

[ElasticsearchHdfs](ElasticsearchHdfs.md "ElasticsearchHdfs" )

[HdfsElasticsearch](HdfsElasticsearch.md "HdfsElasticsearch" )

Build:
---------

`mvn clean package`

Deploy:
--------

`mvn -Pdocker clean package docker:build`

