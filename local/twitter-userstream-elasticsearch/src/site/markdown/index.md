Apache Streams (incubating)
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

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

<a href="TwitterUserstreamElasticsearch.dot.svg" target="_self">TwitterUserstreamElasticsearch.dot.svg</a>

Example Configuration:
----------------------

    twitter {
        endpoint = "userstream"
        oauth {
                consumerKey = "bcg14JThZEGoZ3MZOoT2HnJS7"
                consumerSecret = "S4dwxnZni58CIJaoupGnUrO4HRHmbBGOb28W6IqOJBx36LPw2z"
                accessToken = ""
                accessTokenSecret = ""
        }
    }
    elasticsearch {
        hosts = [
            localhost
        ]
        port = 9300
        clusterName = elasticsearch
        index = userstream_activity
        type = activity
        batchSize = 1
    }

The consumerKey and consumerSecret are assigned when creating a twitter application:



The accessToken and accessTokenSecret can be obtained by navigating to:

    https://api.twitter.com/oauth/authenticate?oauth_token=UIJ0AUxCJatpKDUyFt0OTSEP4asZgqxRwUCT0AMSwc&oauth_callback=http%3A%2F%2Foauth.streamstutorial.w2odata.com%3A8080%2Fsocialauthdemo%2FsocialAuthSuccessAction.do

Build:
---------

`mvn clean package verify`

Run:
--------

`java -cp target/twitter-userstream-elasticsearch-0.2-incubating-SNAPSHOT.jar -Dconfig.file=src/main/resources/application.json org.apache.streams.example.twitter.TwitterUserstreamElasticsearch`

Deploy:
--------
`mvn -Pdocker clean package docker:build`

`docker tag twitter-userstream-elasticsearch:0.2-incubating-SNAPSHOT <dockerregistry>:twitter-userstream-elasticsearch:0.2-incubating-SNAPSHOT`

`docker push <dockerregistry>:twitter-userstream-elasticsearch:0.2-incubating-SNAPSHOT`

`docker run <dockerregistry>:twitter-userstream-elasticsearch:0.2-incubating-SNAPSHOT.jar java -cp twitter-userstream-elasticsearch-0.2-incubating-SNAPSHOT.jar -Dconfig.file=http://<location_of_config_file>.json org.apache.streams.example.twitter.TwitterUserstreamElasticsearch`
