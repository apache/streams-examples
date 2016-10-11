### HdfsElasticsearch

#### Description:

Copies documents from hdfs to elasticsearch.

#### Configuration:

[HdfsElasticsearchIT.conf](HdfsElasticsearchIT.conf "HdfsElasticsearchIT.conf" )

#### Run (SBT):

    sbtx -210 -sbt-create
    set resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
    set libraryDependencies += "org.apache.streams" % "elasticsearch-hdfs" % "0.4-incubating-SNAPSHOT"
    set fork := true
    set javaOptions +="-Dconfig.file=HdfsElasticsearchIT.conf"
    run elasticsearch-hdfs org.apache.streams.example.ElasticsearchHdfs

#### Run (Docker):

    docker run elasticsearch-hdfs java -cp elasticsearch-hdfs-jar-with-dependencies.jar -Dconfig.file=`pwd`/HdfsElasticsearchIT.conf org.apache.streams.example.HdfsElasticsearch

#### Specification:

[HdfsElasticsearch.dot](HdfsElasticsearch.dot "HdfsElasticsearch.dot" )

#### Diagram:

![HdfsElasticsearch.dot.svg](./HdfsElasticsearch.dot.svg)

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0