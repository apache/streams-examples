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

<a href="HdfsElasticsearch.html" target="_self">HdfsElasticsearch</a>

<a href="ElasticsearchHdfs.html" target="_self">ElasticsearchHdfs</a>

Build:
---------

    mvn clean install

Testing:
---------

Start up elasticsearch with docker:
     
    mvn -PdockerITs docker:start
 
Build with integration testing enabled, using your credentials
 
    mvn clean test verify -DskipITs=false -DargLine="-Dconfig.file=`pwd`/application.conf"
 
Shutdown elasticsearch when finished:
 
    mvn -PdockerITs docker:stop

Deploy (Docker):
----------------

    mvn -Pdocker -Ddocker.repo=<your docker host>:<your docker repo> clean package docker:build docker:push

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0