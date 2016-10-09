twitter-userstream-elasticsearch
==============================

Requirements:
-------------
 - Authorized Twitter API credentials
 - A running ElasticSearch 1.0.0+ instance

Description:
------------
This example connects to an active twitter account and stores the userstream as activities in Elasticsearch

Specification:
-----------------

[TwitterUserstreamElasticsearch.dot](TwitterUserstreamElasticsearch.dot "TwitterUserstreamElasticsearch.dot" )

Diagram:
-----------------

![TwitterUserstreamElasticsearch.dot.svg](./TwitterUserstreamElasticsearch.dot.svg)

Example Configuration:
----------------------

[application.conf](application.conf "application.conf" )

The consumerKey and consumerSecret are set for our streams-example application
The accessToken and accessTokenSecret can be obtained by navigating to:

    https://api.twitter.com/oauth/authenticate?oauth_token=UIJ0AUxCJatpKDUyFt0OTSEP4asZgqxRwUCT0AMSwc&oauth_callback=http%3A%2F%2Foauth.streamstutorial.w2odata.com%3A8080%2Fsocialauthdemo%2FsocialAuthSuccessAction.do

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
    
Build with integration testing enabled, using your credentials

    mvn -PdockerITs docker:start
    mvn clean test verify -DskipITs=false -DargLine="-Dconfig.file=`pwd`/application.conf"
    mvn -PdockerITs docker:stop
        
Run (Local):
------------

    java -cp dist/twitter-userstream-elasticsearch-jar-with-dependencies.jar -Dconfig.file=file://<location_of_config_file> org.apache.streams.example.twitter.TwitterUserstreamElasticsearch

Deploy (Docker):
----------------

    mvn -Pdocker -Ddocker.repo=<your docker host>:<your docker repo> docker:build docker:push

Run (Docker):
-------------

    docker run twitter-userstream-elasticsearch java -cp twitter-userstream-elasticsearch-jar-with-dependencies.jar -Dconfig.url=http://<location_of_config_file> org.apache.streams.example.twitter.TwitterUserstreamElasticsearch

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
