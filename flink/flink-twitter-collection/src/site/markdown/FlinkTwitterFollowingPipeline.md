FlinkTwitterFollowingPipeline
=============================

Description:
-----------------

Collects twitter friends or followers with flink.

Specification:
-----------------

[FlinkTwitterFollowingPipeline.dot](FlinkTwitterFollowingPipeline.dot "FlinkTwitterFollowingPipeline.dot" )

Diagram:
-----------------

![FlinkTwitterFollowingPipeline.dot.svg](./FlinkTwitterFollowingPipeline.dot.svg)

Example Configuration:
----------------------

[FlinkTwitterFollowingPipelineFollowersIT.conf](FlinkTwitterFollowingPipelineFollowersIT.conf "FlinkTwitterFollowingPipelineFollowersIT.conf" )

[FlinkTwitterFollowingPipelineFriendsIT.conf](FlinkTwitterFollowingPipelineFriendsIT.conf "FlinkTwitterFollowingPipelineFriendsIT.conf" )

Run (Local):
------------

    java -cp dist/flink-twitter-collection-jar-with-dependencies.jar -Dconfig.file=file://<location_of_config_file> org.apache.streams.examples.flink.twitter.collection.FlinkTwitterFollowingPipeline

Run (Flink):
------------

    flink-run.sh dist/flink-twitter-collection-jar-with-dependencies.jar org.apache.streams.examples.flink.twitter.collection.FlinkTwitterFollowingPipeline http://<location_of_config_file> 

Run (YARN):
-----------

    flink-run.sh yarn dist/flink-twitter-collection-jar-with-dependencies.jar org.apache.streams.examples.flink.twitter.collection.FlinkTwitterFollowingPipeline http://<location_of_config_file> 

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0