%spark.dep
z.reset()
z.addRepo("apache-snapshots").url("https://repository.apache.org/content/repositories/snapshots").snapshot()
z.load("org.apache.streams:streams-provider-facebook:0.4-incubating-SNAPSHOT")

%spark
import com.typesafe.config._
import org.apache.streams.config._
import org.apache.streams.core._
import org.apache.streams.facebook._
import org.apache.streams.facebook.graph._
import java.util.Iterator

%spark
val credentials =
  """
    |facebook {
    |  oauth {
    |    appId = "299258633581961"
    |    appSecret = 03b887d68ee4a3117f9f087330fe8c8f
    |  }
    |  userAccessTokens = [
    |EAACEdEose0cBAG4nq7ZB36wwCGv14UToDpZCwXgZA1ZCuShBp1tPQozsbxU75RaOEiJKx75sQgox6wCNgx6rCrEL5K96oNE9EoGutFPBPAEWBZAo7xlgfx715HhAdqdmoaaFTbwJWwruehr1FwIXJr2OAfsxFrqYbPYUkXXojAtSgoEm9WrhW6RRa7os6xBIZD
    |  ]
    |}
    |"""
val credentialsConfig = ConfigFactory.parseString(credentials)

%spark
val accounts =
  """
    |facebook {
    |  ids = [
    |    {
    |      #"id": "Apache-Software-Foundation"
    |      "id": "108021202551732"
    |    },
    |    {
    |      #"id": "Apache-Spark"
    |      "id": "695067547183193"
    |    },
    |    {
    |      # Apache-Cordova
    |      "id": "144287225588642"
    |    },
    |    {
    |      # Apache-HTTP-Server
    |      "id": "107703115926025"
    |    },
    |    {
    |      # Apache-Cassandra
    |      "id": "136080266420061"
    |    },
    |    {
    |      # Apache-Solr
    |      "id": "333596995194"
    |    },
    |    {
    |      # Apache-CXF
    |      "id": "509899489117171"
    |    },
    |    {
    |      # Apache-Kafka
    |      "id": "109576742394607"
    |    },
    |    {
    |      # Apache-Groovy
    |      "id": "112510602100049"
    |    },
    |    {
    |      # Apache-Hadoop
    |      "id": "102175453157656"
    |    },
    |    {
    |      # Apache-Hive
    |      "id": "192818954063511"
    |    },
    |    {
    |      # Apache-Mahout
    |      "id": "109528065733066"
    |    },
    |    {
    |      # Apache-HBase
    |      "id": "103760282995363"
    |    }
    |  ]
    |}
    |"""
val accountsConfig = ConfigFactory.parseString(accounts)

%spark
val reference = ConfigFactory.load()
val typesafe = accountsConfig.withFallback(credentialsConfig).withFallback(reference).resolve()
val config = new ComponentConfigurator(classOf[FacebookUserInformationConfiguration]).detectConfiguration(typesafe, "facebook");

%spark
// Pull info on those accounts
val FacebookPageProvider = new FacebookPageProvider(config);
FacebookPageProvider.prepare(null)
FacebookPageProvider.startStream()
//
val userdata_buf = scala.collection.mutable.ArrayBuffer.empty[Object]
while(FacebookPageProvider.isRunning()) {
  val resultSet = FacebookPageProvider.readCurrent()
  resultSet.size()
  val iterator = resultSet.iterator();
  while(iterator.hasNext()) {
    val datum = iterator.next();
    userdata_buf += datum.getDocument
  }
}

%spark
//Pull activity from those accounts
val FacebookPageFeedProvider = new FacebookPageFeedProvider(config);
FacebookPageFeedProvider.prepare(null)
FacebookPageFeedProvider.startStream()
while(FacebookPageFeedProvider.isRunning())
//
val useractivity_buf = scala.collection.mutable.ArrayBuffer.empty[Object]
while(FacebookPageFeedProvider.isRunning()) {
  val resultSet = FacebookPageFeedProvider.readCurrent()
  resultSet.size()
  val iterator = resultSet.iterator();
  while(iterator.hasNext()) {
    val datum = iterator.next();
    useractivity_buf += datum.getDocument
  }
}

%spark
//Normalize person(s) -> page(s)
val FacebookTypeConverter = new FacebookTypeConverter(classOf[Page], classOf[Page])
FacebookTypeConverter.prepare()
val userdata_pages = userdata_buf.flatMap(x => FacebookTypeConverter.process(x))

%spark
//Normalize activities) -> posts(s)
val FacebookTypeConverter = new FacebookTypeConverter(classOf[Post], classOf[Post])
FacebookTypeConverter.prepare()
val useractivity_posts = useractivity_buf.flatMap(x => FacebookTypeConverter.process(x))


