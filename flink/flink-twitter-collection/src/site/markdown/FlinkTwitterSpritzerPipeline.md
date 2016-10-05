FlinkTwitterSpritzerPipeline
============================

Description:
-----------------

Collects twitter posts in real-time from the sample endpoint with flink.

Specification:
-----------------

[FlinkTwitterSpritzerPipeline.dot](FlinkTwitterSpritzerPipeline.dot "FlinkTwitterSpritzerPipeline.dot" )

Diagram:
-----------------

![FlinkTwitterSpritzerPipeline.dot.svg](./FlinkTwitterSpritzerPipeline.dot.svg)

Example Configuration:
----------------------

[FlinkTwitterSpritzerPipelineIT.conf](FlinkTwitterSpritzerPipelineIT.conf "FlinkTwitterSpritzerPipelineIT.conf" )

Run (Local):
------------

    java -cp dist/flink-twitter-collection-jar-with-dependencies.jar -Dconfig.file=file://<location_of_config_file> org.apache.streams.examples.flink.twitter.collection.FlinkTwitterSpritzerPipeline

Run (Flink):
------------

    flink-run.sh dist/flink-twitter-collection-jar-with-dependencies.jar org.apache.streams.examples.flink.twitter.collection.FlinkTwitterSpritzerPipeline http://<location_of_config_file> 

Run (YARN):
-----------

    flink-run.sh yarn dist/flink-twitter-collection-jar-with-dependencies.jar org.apache.streams.examples.flink.twitter.collection.FlinkTwitterSpritzerPipeline http://<location_of_config_file> 

[JavaDocs](apidocs/index.html "JavaDocs")

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0