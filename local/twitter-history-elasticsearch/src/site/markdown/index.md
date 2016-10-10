twitter-history-elasticsearch
==============================

Requirements:
-------------
 - Authorized Twitter API credentials
 - A running ElasticSearch 1.0.0+ instance

Description:
------------
Retrieves as many posts from a known list of users as twitter API allows.

Converts them to activities, and writes them in activity format to Elasticsearch.

Specification:
-----------------

[TwitterHistoryElasticsearch.dot](TwitterHistoryElasticsearch.dot "TwitterHistoryElasticsearch.dot" )

Diagram:
-----------------

![TwitterHistoryElasticsearch.dot.svg](./TwitterHistoryElasticsearch.dot.svg)

Example Configuration:
----------------------

[application.conf](application.conf "application.conf" )

In the Twitter section you should place all of your relevant authentication keys and whichever Twitter IDs you want to pull history for.

Twitter IDs can be converted from screennames at http://www.gettwitterid.com

Build:
---------

    mvn clean package
   
Testing:
---------

Create a local file `application.conf` with valid twitter credentials

    twitter {
      oauth {
        consumerKey = ""
        consumerSecret = ""
        accessToken = ""
        accessTokenSecret = ""
      }
    }
    
Start up elasticsearch with docker:
    
        mvn -PdockerITs docker:start

Build with integration testing enabled, using your credentials

    mvn clean test verify -DskipITs=false -DargLine="-Dconfig.file=`pwd`/application.conf"

Shutdown elasticsearch when finished:

    mvn -PdockerITs docker:stop

Run (Local):
------------

    java -cp dist/twitter-history-elasticsearch-jar-with-dependencies.jar -Dconfig.file=file://<location_of_config_file> org.apache.streams.example.twitter.TwitterHistoryElasticsearch

Deploy (Docker):
----------------

    mvn -Pdocker -Ddocker.repo=<your docker host>:<your docker repo> docker:build docker:push

Run (Docker):
-------------

    docker run twitter-history-elasticsearch java -cp twitter-history-elasticsearch-jar-with-dependencies.jar -Dconfig.url=http://<location_of_config_file> org.apache.streams.example.twitter.TwitterHistoryElasticsearch

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
