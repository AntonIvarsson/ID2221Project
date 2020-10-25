import java.util.{Date, Properties}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, ProducerConfig}
import scala.util.Random
import kafka.producer.KeyedMessage
import scala.collection.mutable.ListBuffer

object ScalaProducerExample extends App {
    def getRandomMessage: String = {
        count = count + 1
        if(! src.hasNext) {
            src = scala.io.Source.fromFile("dataset_smol.csv").getLines.map(_.split(",")(2))
        }
        if(count % 2500 == 0) {
            x_mean = rnd.nextInt(3480)
            y_mean = rnd.nextInt(2470)
        }
        var line = src.next().toString
        var x = (rnd.nextGaussian()* x_var + x_mean).toString
        var y = (rnd.nextGaussian()* y_var + y_mean).toString
        var messages : String = ""
    
        if(rnd.nextDouble() > (1 - Covid_prob)) {
            var i = rnd.nextInt(line.length);
            messages = x + ", " + y +", " + line.slice(1, i) + " " + covid_list(rnd.nextInt(covid_list.length)) + " " + line.slice(i,line.length)
        } else { messages = x + ", " + y +", " + line }
        messages
    }


    val rnd = new Random()

    var count = 0 
    val Covid_prob = 0.2
    var x_mean = rnd.nextInt(3480)
    val x_var  = 50
    var y_mean = rnd.nextInt(2470)
    val y_var  = 50
    
    var covid_list = scala.io.Source.fromFile("covidlist.csv").getLines.toList
    var src = scala.io.Source.fromFile("dataset_smol.csv").getLines.map(_.split(",")(2))

    val events = 10000
    val topic = "covid"
    val brokers = "localhost:9092"


    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "ScalaProducerExample")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](props)

    while (true) {
        
        val data = new ProducerRecord[String, String](topic, null, getRandomMessage)
        producer.send(data)
        print(data + "\n")
    }

    producer.close()
}
