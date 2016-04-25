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

![ElasticsearchHdfs.dot.svg](./ElasticsearchHdfs.dot.svg)

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

`docker run elasticsearch-hdfs:0.2-incubating-SNAPSHOT-PP.jar java -cp stash-migrate-0.2-incubating-SNAPSHOT.jar -Dconfig.file=http://<location_of_config_file>.json org.apache.streams.elasticsearch.example.HdfsElasticsearch`
