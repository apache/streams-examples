### ElasticsearchHdfs

#### Description:

Copies documents from elasticsearch to hdfs.

#### Configuration:

[ElasticsearchHdfsIT.conf](ElasticsearchHdfsIT.conf "ElasticsearchHdfsIT.conf" )

#### Run (SBT):

    sbtx -210 -sbt-create
    set resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
    set libraryDependencies += "org.apache.streams" % "elasticsearch-hdfs" % "0.4-incubating-SNAPSHOT"
    set fork := true
    set javaOptions +="-Dconfig.file=application.conf"
    run elasticsearch-hdfs org.apache.streams.example.ElasticsearchHdfs

#### Run (Docker):

    docker run apachestreams/elasticsearch-hdfs java -cp elasticsearch-hdfs-jar-with-dependencies.jar -Dconfig.url=http://<location_of_config_file> org.apache.streams.example.ElasticsearchHdfs

#### Specification:

[ElasticsearchHdfs.dot](ElasticsearchHdfs.dot "ElasticsearchHdfs.dot" )

#### Diagram:

![ElasticsearchHdfs.dot.svg](./ElasticsearchHdfs.dot.svg)

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0