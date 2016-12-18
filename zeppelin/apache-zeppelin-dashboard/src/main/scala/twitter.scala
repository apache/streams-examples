/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

%spark.dep
z.reset()
z.addRepo("apache-snapshots").url("https://repository.apache.org/content/repositories/snapshots").snapshot()
z.load("org.apache.streams:streams-provider-twitter:0.4-incubating-SNAPSHOT")

%spark
import com.typesafe.config._
import org.apache.streams.config._
import org.apache.streams.core._
import java.util.Iterator
import org.apache.streams.twitter.TwitterUserInformationConfiguration
import org.apache.streams.twitter.pojo._
import org.apache.streams.twitter.provider._

%spark
val consumerKey = z.input("ConsumerKey", "jF3awfLECUZ4tFAwS5bZcha8c")
val consumerSecret = z.input("ConsumerSecret", "0IjoS5aPE88kNSREK6HNzAhUcJMziSlaT1fOkA5pzpusZLrhCj")
val accessToken = z.input("AccessToken", "42232950-CzaYlt2M6SPGI883B5NZ8vROcL4qUsTJlp9wIM2K2")
val accessTokenSecret = z.input("AccessTokenSecret", "vviQzladFUl23hdVelEiIknSLoHfAs40DqTv3RdXHhmz0")

%spark
val credentials_hocon = s"""
    twitter {
      oauth {
       consumerKey = "$consumerKey"
    consumerSecret = "$consumerSecret"
    accessToken = "$accessToken"
    accessTokenSecret = "$accessTokenSecret"
      }
      retrySleepMs = 5000
  retryMax = 250
    }
"""

%spark
val accounts_hocon = s"""
twitter.info = [
#    "ApacheSpark"
    1551361069
#    "ApacheFlink"
    2493948216
#    "ApacheKafka"
    1287555762
#   "Hadoop"
    175671289
#   "ApacheCassandra"
    19041500
#   "ApacheSolr"
    22742048
#   "ApacheMahout"
    1123330975
#   "ApacheHive"
    1188044936
#   "ApacheHbase"
    2912997159
]
"""

%spark
val reference = ConfigFactory.load()
val credentials = ConfigFactory.parseString(credentials_hocon)
val accounts = ConfigFactory.parseString(accounts_hocon)
val typesafe = accounts.withFallback(credentials).withFallback(reference).resolve()
val twitterUserInformationConfiguration = new ComponentConfigurator(classOf[TwitterUserInformationConfiguration]).detectConfiguration(typesafe, "twitter");

%spark
val userdata_buf = scala.collection.mutable.ArrayBuffer.empty[Object]

val twitterUserInformationProvider = new TwitterUserInformationProvider(twitterUserInformationConfiguration);
twitterUserInformationProvider.prepare()
twitterUserInformationProvider.startStream()
while(twitterUserInformationProvider.isRunning()) {
  val resultSet = twitterUserInformationProvider.readCurrent()
  resultSet.size()
  val iterator = resultSet.iterator();
  while(iterator.hasNext()) {
    val datum = iterator.next();
    //println(datum.getDocument)
    userdata_buf += datum.getDocument
  }
}
userdata_buf.size

%spark
import com.typesafe.config._
import org.apache.streams.config._
import org.apache.streams.core._
import java.util.Iterator
import org.apache.streams.twitter.TwitterUserInformationConfiguration

import org.apache.streams.twitter.pojo._
import org.apache.streams.twitter.provider._

val timeline_buf = scala.collection.mutable.ArrayBuffer.empty[Object]

val twitterTimelineProvider = new TwitterTimelineProvider(twitterUserInformationConfiguration);
twitterTimelineProvider.prepare(twitterUserInformationConfiguration)
twitterTimelineProvider.startStream()
while(twitterTimelineProvider.isRunning()) {
  val resultSet = twitterTimelineProvider.readCurrent()
  resultSet.size()
  val iterator = resultSet.iterator();
  while(iterator.hasNext()) {
    val datum = iterator.next();
    //println(datum.getDocument)
    timeline_buf += datum.getDocument
  }
}
timeline_buf.size

%spark
import org.apache.streams.converter.ActivityObjectConverterProcessor
import org.apache.streams.core.StreamsProcessor
import org.apache.streams.pojo.json.ActivityObject
import scala.collection.JavaConverters
import scala.collection.JavaConversions._

val converter = new ActivityObjectConverterProcessor()
converter.prepare()

val user_datums = userdata_buf.map(x => new StreamsDatum(x))
val actor_datums = user_datums.flatMap(x => converter.process(x))
val pages = actor_datums.map(x => x.getDocument.asInstanceOf[ActivityObject])

%spark
import org.apache.streams.jackson.StreamsJacksonMapper;
import sqlContext._
import sqlContext.implicits._

val mapper = StreamsJacksonMapper.getInstance();
val pages_jsons = pages.map(o => mapper.writeValueAsString(o))
val pagesRDD = sc.parallelize(pages_jsons)

val pagesDF = sqlContext.read.json(pagesRDD)

val pagescleanDF = pagesDF.withColumn("summary", removePunctuationAndSpecialChar(pagesDF("summary")))
pagescleanDF.registerTempTable("twitter_pages")
pagescleanDF.printSchema

%spark
import org.apache.streams.converter.ActivityConverterProcessor
import org.apache.streams.core.StreamsProcessor
import org.apache.streams.pojo.json.Activity
import scala.collection.JavaConverters
import scala.collection.JavaConversions._

val converter = new ActivityConverterProcessor()
converter.prepare()

val status_datums = timeline_buf.map(x => new StreamsDatum(x))
val activity_datums = status_datums.flatMap(x => converter.process(x)).map(x => x.getDocument.asInstanceOf[Activity])
activity_datums.size

%spark
import org.apache.streams.jackson.StreamsJacksonMapper;
import sqlContext._
import sqlContext.implicits._

val mapper = StreamsJacksonMapper.getInstance();
val jsons = activity_datums.map(o => mapper.writeValueAsString(o))
val activitiesRDD = sc.parallelize(jsons)

val activitiesDF = sqlContext.read.json(activitiesRDD)

val cleanDF = activitiesDF.withColumn("content", removePunctuationAndSpecialChar(activitiesDF("content")))
cleanDF.registerTempTable("twitter_posts")
cleanDF.printSchema

%spark.sql
select id, displayName, handle, summary, extensions.favorites, extensions.followers, extensions.posts from twitter_pages

%spark.sql
select id, actor.id, content, published, rebroadcasts.count from twitter_posts
