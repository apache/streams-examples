Apache Streams (incubating)
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

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

<a href="ElasticsearchHdfs.dot.svg" target="_self">ElasticsearchHdfs.dot.svg</a>

Configuration:
-----------------

[ElasticsearchHdfsConfiguration.json](ElasticsearchHdfsConfiguration.json "ElasticsearchHdfsConfiguration.json" )

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

Run:
--------

    java -cp dist/elasticsearch-hdfs-0.2-incubating-jar-with-dependencies.jar -Dconfig.file=`pwd`/src/test/resources/testBackup.json org.apache.streams.elasticsearch.example.HdfsElasticsearch

    docker run elasticsearch-hdfs:0.2-incubating java -cp /elasticsearch-hdfs-0.2-incubating-jar-with-dependencies.jar -Dconfig.url=http://<location_of_config_resource> org.apache.streams.elasticsearch.example.HdfsElasticsearch
    