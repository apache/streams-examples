Apache Streams (incubating)
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

mongo-elasticsearch-sync
==============================

Requirements:
-------------
 - A running MongoDB 2.4+ instance
 - A running ElasticSearch 1.0.0+ instance

Description:
------------
Copies documents from mongodb to elasticsearch

Specification:
-----------------

[MongoElasticsearchSync.dot](src/main/resources/MongoElasticsearchSync.dot "MongoElasticsearchSync.dot" )

Diagram:
-----------------

![MongoElasticsearchSync.png](./MongoElasticsearchSync.png?raw=true)

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

Build:
---------

`mvn clean package verify`

Run:
--------

`java -cp target/mongo-elasticsearch-sync-0.1-SNAPSHOT.jar -Dconfig.file=src/main/resources/application.json org.apache.streams.example.elasticsearch.MongoElasticsearchSync`

Deploy:
--------

`mvn -Pdocker clean package docker:build`

`docker tag mongo-elasticsearch-sync:0.2-incubating-SNAPSHOT <dockerregistry>:mongo-elasticsearch-sync:0.2-incubating-SNAPSHOT`

`docker push <dockerregistry>:mongo-elasticsearch-sync:0.2-incubating-SNAPSHOT`

`docker run <dockerregistry>:mongo-elasticsearch-sync:0.2-incubating-SNAPSHOT java -cp mongo-elasticsearch-sync-0.2-incubating-SNAPSHOT.jar -Dconfig.file=http://<location_of_config_file>.json org.apache.streams.example.elasticsearch.MongoElasticsearchSync`

