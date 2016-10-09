FlinkTwitterUserInformationPipeline
===================================

Description:
-----------------

Collects twitter user profiles with flink.

Specification:
-----------------

[FlinkTwitterUserInformationPipeline.dot](FlinkTwitterUserInformationPipeline.dot "FlinkTwitterUserInformationPipeline.dot" )

Diagram:
-----------------

![TwitterUserInformationPipeline.dot.svg](./TwitterUserInformationPipeline.dot.svg)

Example Configuration:
----------------------

[FlinkTwitterUserInformationPipelineIT.conf](FlinkTwitterUserInformationPipelineIT.conf "FlinkTwitterUserInformationPipelineIT.conf" )

Run (Local):
------------

    java -cp dist/flink-twitter-collection-jar-with-dependencies.jar -Dconfig.file=file://<location_of_config_file> org.apache.streams.examples.flink.twitter.collection.FlinkTwitterUserInformationPipeline

Run (Flink):
------------

    flink-run.sh dist/flink-twitter-collection-jar-with-dependencies.jar org.apache.streams.examples.flink.twitter.collection.FlinkTwitterUserInformationPipeline http://<location_of_config_file> 

Run (YARN):
-----------

    flink-run.sh yarn dist/flink-twitter-collection-jar-with-dependencies.jar org.apache.streams.examples.flink.twitter.collection.FlinkTwitterUserInformationPipeline http://<location_of_config_file> 

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0