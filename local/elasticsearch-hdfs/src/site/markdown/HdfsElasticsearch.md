Apache Streams (incubating)
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

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

<a href="HdfsElasticsearch.dot.svg" target="_self">HdfsElasticsearch.dot.svg</a>

Example Configuration:
----------------------

    {
        "source": {
            "scheme": "file",
            "host": "localhost",
            "user": "cloudera",
            "path": "/tmp",
            "writerPath": "activity"
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


Run:
--------

`docker run elasticsearch-hdfs:0.2-incubating-SNAPSHOT.jar java -cp elasticsearch-hdfs-0.2-incubating-SNAPSHOT.jar -Dconfig.file=file://<location_of_config_file>.json org.apache.streams.elasticsearch.example.HdfsElasticsearch`
