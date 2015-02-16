Apache Streams Examples (incubating)
Licensed under Apache License 2.0 - http://www.apache.org/licenses/LICENSE-2.0
--------------------------------------------------------------------------------

About
=====
Apache Streams Examples (incubating) contains community-supported example applications built on Apache Streams (incubating).

Apache Streams is a lightweight (yet scalable) server for ActivityStreams. The role of Apache Streams is to provide a central point of aggregation, filtering and querying for Activities that have been submitted by disparate systems. Apache Streams also intends to include a mechanism for intelligent filtering and recommendation to reduce the noise to end users.

Release Notes
=============


Getting Started
===============
Please visit the project website for the latest information:
    http://streams.incubator.apache.org/

Along with the developer mailing list archive:
    http://mail-archives.apache.org/mod_mbox/streams-dev/

System Requirements
===================
You need a platform that supports Java SE 7 or later.

Building and running
====================
To build from source code:

  - Requirements:
    Sources compilation require Java SE 7 or higher.
    The project is built with Apache Maven 3+ (suggested is 3.2.1).
    You need to download and install Maven 3 from: http://maven.apache.org/

  - The Streams Examples project itself (this one) depends on the separate Streams project
    which contains the source code and poms for Apache Streams.
    As streams-project is already published to the Apache Releases repository,
    there is no need to check it out manually and build it locally yourself,
    unless you choose to checkout a SNAPSHOT branch.
    
    If so needed, incubator-streams can be checked out from:
      http://git-wip-us.apache.org/repos/asf/incubator-streams-examples.git

    After check out, cd into incubator-streams and invoke maven to install it using:
      $mvn install
    
  - To build all of the Streams examples, invoke maven in the root directory:
      $mvn install

  - To build and install a docker image containing a specific example, change to that example's directory then:
      $mvn -Pdocker clean package docker:build

    This will only work if you have a working docker installation.
