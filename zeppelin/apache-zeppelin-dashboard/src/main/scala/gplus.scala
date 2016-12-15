



%spark
val reference = ConfigFactory.load()
val typesafe = accountsConfig.withFallback(credentialsConfig).withFallback(reference).resolve()
val config = new ComponentConfigurator(classOf[GPlusConfiguration]).detectConfiguration(typesafe, "gplus");

%spark
// Pull info on those accounts
val GPlusUserDataProvider = new GPlusUserDataProvider(config);
GPlusUserDataProvider.prepare(null)
GPlusUserDataProvider.startStream()
//
val userdata_buf = scala.collection.mutable.ArrayBuffer.empty[Object]
while(GPlusUserDataProvider.isRunning()) {
  val resultSet = GPlusUserDataProvider.readCurrent()
  resultSet.size()
  val iterator = resultSet.iterator();
  while(iterator.hasNext()) {
    val datum = iterator.next();
    userdata_buf += datum.getDocument
  }
}

%spark
//Pull activity from those accounts
val GPlusUserActivityProvider = new GPlusUserActivityProvider(config);
GPlusUserActivityProvider.prepare(null)
GPlusUserActivityProvider.startStream()
while(GPlusUserActivityProvider.isRunning())
//
val useractivity_buf = scala.collection.mutable.ArrayBuffer.empty[Object]
while(GPlusUserActivityProvider.isRunning()) {
  val resultSet = GPlusUserActivityProvider.readCurrent()
  resultSet.size()
  val iterator = resultSet.iterator();
  while(iterator.hasNext()) {
    val datum = iterator.next();
    useractivity_buf += datum.getDocument
  }
}

%spark
//Normalize person(s) -> page(s)
val GooglePlusTypeConverter = new GooglePlusTypeConverter()
GooglePlusTypeConverter.prepare()
val userdata_pages = userdata_buf.flatMap(x => GooglePlusTypeConverter.process(x))


%spark
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.sun.javafx.runtime.async.AbstractAsyncOperation
import sun.jvm.hotspot.runtime.NativeSignatureIterator
class MyExclusionStrategy extends ExclusionStrategy {
  def shouldSkipField(f: FieldAttributes) : Boolean {
    f.getName().toLowerCase().contains("additionalProperties");
  }
}

//Normalize activities) -> posts(s)
val GooglePlusTypeConverter = new GooglePlusTypeConverter()
GooglePlusTypeConverter.prepare()
val useractivity_posts = useractivity_buf.flatMap(x => GooglePlusTypeConverter.process(x))


