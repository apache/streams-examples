### TwitterUserstreamElasticsearch

#### Description:

This example connects to an active twitter account and stores the userstream as activities in Elasticsearch

#### Configuration:

[TwitterUserstreamElasticsearchIT.conf](TwitterUserstreamElasticsearchIT.conf "TwitterUserstreamElasticsearchIT.conf" )

#### Run (SBT):

    sbtx -210 -sbt-create
    set resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
    set libraryDependencies += "org.apache.streams" % "twitter-userstream-elasticsearch" % "0.4-incubating-SNAPSHOT"
    set fork := true
    set javaOptions +="-Dconfig.file=application.conf"
    run org.apache.streams.example.TwitterUserstreamElasticsearch

#### Run (Docker):

    docker run apachestreams/twitter-userstream-elasticsearch java -cp twitter-userstream-elasticsearch-jar-with-dependencies.jar -Dconfig.file=`pwd`/application.conf org.apache.streams.example.TwitterUserstreamElasticsearch

#### Specification:

[TwitterUserstreamElasticsearch.dot](TwitterUserstreamElasticsearch.dot "TwitterUserstreamElasticsearch.dot" )

#### Diagram:

![TwitterUserstreamElasticsearch.dot.svg](./TwitterUserstreamElasticsearch.dot.svg)

###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
