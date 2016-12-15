%spark.dep
z.reset()
z.addRepo("apache-snapshots").url("https://repository.apache.org/content/repositories/snapshots").snapshot()
z.load("org.apache.streams:streams-provider-youtube:0.4-incubating-SNAPSHOT")

%spark
import com.typesafe.config._
import org.apache.streams.config._
import org.apache.streams.core._
import com.youtube.provider._
import org.apache.youtube.pojo._
import java.util.Iterator

%spark
val credentials =
  """
  |youtube {
  |  apiKey = 79d9f9ca2796d1ec5334faf8d6efaa6456a297e6
  |  oauth {
  |    serviceAccountEmailAddress = "streamsdev@adroit-particle-764.iam.gserviceaccount.com"
  |    pathToP12KeyFile = streams-c84fa47bd759.p12
  |  }
  |}
  |"""
val credentialsConfig = ConfigFactory.parseString(credentials)

%spark
val accounts =
  """
    |youtube {
    |  youtubeUsers = [
    |    {
    |      userId = "UCLDJ_V9KUOdOFSbDvPfGBxw"
    |    }
    |  ]
    |}
    |"""
val accountsConfig = ConfigFactory.parseString(accounts)

%spark
val reference = ConfigFactory.load()
val typesafe = accountsConfig.withFallback(credentialsConfig).withFallback(reference).resolve()
val config = new ComponentConfigurator(classOf[YoutubeConfiguration]).detectConfiguration(typesafe, "youtube");

%spark
// Pull info on those channels
val YoutubeChannelProvider = new YoutubeChannelProvider(config);
YoutubeChannelProvider.prepare(null)
YoutubeChannelProvider.startStream()
//
val channel_buf = scala.collection.mutable.ArrayBuffer.empty[Object]
while(YoutubeChannelProvider.isRunning()) {
  val resultSet = YoutubeChannelProvider.readCurrent()
  resultSet.size()
  val iterator = resultSet.iterator();
  while(iterator.hasNext()) {
    val datum = iterator.next();
    channel_buf += datum.getDocument
  }
}

%spark
//Pull activity from those accounts
val YoutubeUserActivityProvider = new YoutubeUserActivityProvider(config);
YoutubeUserActivityProvider.prepare(null)
YoutubeUserActivityProvider.startStream()
while(YoutubeUserActivityProvider.isRunning())
//
val useractivity_buf = scala.collection.mutable.ArrayBuffer.empty[Object]
while(YoutubeUserActivityProvider.isRunning()) {
  val resultSet = YoutubeUserActivityProvider.readCurrent()
  resultSet.size()
  val iterator = resultSet.iterator();
  while(iterator.hasNext()) {
    val datum = iterator.next();
    useractivity_buf += datum.getDocument
  }
}

%spark
import org.apache.streams.core.StreamsDatum
import com.youtube.processor._
import scala.collection.JavaConversions._
//Normalize activities -> posts(s)
val YoutubeTypeConverter = new YoutubeTypeConverter()
YoutubeTypeConverter.prepare()
val useractivity_posts = useractivity_buf.flatMap(x => YoutubeTypeConverter.process(x))

%spark
import org.apache.streams.jackson.StreamsJacksonMapper;

val sqlContext = new org.apache.spark.sql.SQLContext(sc)

val mapper = StreamsJacksonMapper.getInstance();
val activitiesRDD = sc.parallelize(useractivity_posts.map(o => mapper.writeValueAsString(o)))

val activitiesDF = sqlContext.read.json(activitiesRDD)

activitiesDF.registerTempTable("activities")

%spark.sql
select count(id) from activitiesDF
