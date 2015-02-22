Apache Streams (incubating)
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

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

[TwitterHistoryElasticsearch.dot](src/main/resources/TwitterHistoryElasticsearch.dot "TwitterHistoryElasticsearch.dot" )

Diagram:
-----------------

![TwitterHistoryElasticsearch.png](./TwitterHistoryElasticsearch.png?raw=true)

Example Configuration:
----------------------

    twitter {
        host = "api.twitter.com"
        endpoint = "statuses/user_timeline"
        oauth {
            consumerKey = ""
            consumerSecret = ""
            accessToken = ""
            accessTokenSecret = ""
        }
        info = [
            "42232950"
            "211620426"
        ]
    }
    elasticsearch {
        hosts = [
            localhost
        ]
        port = 9300
        clusterName = elasticsearch
        index = userhistory_activity
        type = activity
    }

In the Twitter section you should place all of your relevant authentication keys and whichever Twitter IDs you're looking to follow
Twitter IDs can be converted from screennames at http://www.gettwitterid.com

Running:
--------

You will need to run `./install_templates.sh` in the resources folder in order to apply the templates to your ES cluster

    java -cp target/twitter-history-elasticsearch-0.1-SNAPSHOT.jar -Dconfig.file=application.conf org.apache.streams.twitter.example.TwitterHistoryElasticsearchActivity

Note that you must modify src/main/resources/application.conf, and supply an absolute path to config.file

Verification:
-------------
Open up http://localhost:9200/_plugin/head/ and confirm that the index you specified now contains has data

Download https://github.com/w2ogroup/streams-examples/blob/master/twitter-history-elasticsearch/src/main/resources/reports/ActivityReport.json

Open up http://localhost:9200/_plugin/marvel and from the folder icon in the top right hand corner click
    Load -> Advanced -> Choose File and select the report you downloaded

The gear on the top-right allows you to change the report index

You should now see dashboards displaying metrics about your twitter activity