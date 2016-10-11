### TwitterFollowNeo4j

#### Description:

Collects friend or follower connections for a set of twitter users to build a graph database in neo4j.

#### Configuration:

[TwitterFollowNeo4jIT.conf](TwitterFollowNeo4jIT.conf "TwitterFollowNeo4jIT.conf" )

#### Run (SBT):

    sbtx -210 -sbt-create
    set resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
    set libraryDependencies += "org.apache.streams" % "twitter-follow-neo4j" % "0.4-incubating-SNAPSHOT"
    set fork := true
    set javaOptions +="-Dconfig.file=application.conf"
    run org.apache.streams.example.graph.TwitterFollowNeo4j

#### Run (Docker):

    docker run apachestreams/twitter-follow-neo4j java -cp twitter-follow-neo4j-jar-with-dependencies.jar org.apache.streams.example.TwitterFollowNeo4j

#### Specification:

[TwitterFollowNeo4j.dot](TwitterFollowNeo4j.dot "TwitterFollowNeo4j.dot" )

#### Diagram:

![TwitterFollowNeo4j.dot.svg](./TwitterFollowNeo4j.dot.svg)


###### Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
