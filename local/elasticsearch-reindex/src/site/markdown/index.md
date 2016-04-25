Apache Streams (incubating)
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

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

[ElasticsearchReindex.dot](src/main/resources/ElasticsearchReindex.dot "ElasticsearchReindex.dot" )

Diagram:
-----------------

![ElasticsearchReindex.dot.svg](./ElasticsearchReindex.dot.svg)

Example Configuration:
----------------------

    {
        "source": {
            "hosts": [
                "localhost"
            ],
            "port": 9300,
            "clusterName": "elasticsearch",
            "indexes": [
                "activity"
            ],
            "types": [
                "activity"
            ],
            "forceUseConfig": true
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

Populate source and destination in configuration with cluster / index / type details.

Build:
---------

`mvn clean package verify`

Run:
--------

`java -cp target/elasticsearch-reindex-0.1-SNAPSHOT.jar -Dconfig.file=src/main/resources/application.json org.apache.streams.example.elasticsearch.ElasticsearchReindex`

Deploy:
--------
`mvn -Pdocker clean package docker:build`

`docker tag elasticsearch-reindex:0.2-incubating-SNAPSHOT <dockerregistry>:elasticsearch-reindex:0.2-incubating-SNAPSHOT`

`docker push <dockerregistry>:elasticsearch-reindex:0.2-incubating-SNAPSHOT`

`docker run <dockerregistry>:elasticsearch-reindex:0.2-incubating-SNAPSHOT.jar java -cp elasticsearch-reindex-0.2-incubating-SNAPSHOT.jar -Dconfig.file=http://<location_of_config_file>.json org.apache.streams.example.elasticsearch.ElasticsearchReindex`

