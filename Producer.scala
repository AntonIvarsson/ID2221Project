import java.util.{Date, Properties}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, ProducerConfig}
import scala.util.Random
import kafka.producer.KeyedMessage
import scala.collection.mutable.ListBuffer

object ScalaProducerExample extends App {
    def getRandomMessage: String = {
        if(src.hasNext == False) {
            src = file.getLines.map(_.split(",")(2))
        }
        var line = src.next().toString
        var x = (rnd.nextGaussian()* x_var + x_mean).toString
        var y = (rnd.nextGaussian()* y_var + y_mean).toString
        var messages : String = ""
    
        if(random.nextDouble() > (1 - Covid_prob)) {
            var i = rnd.nextInt(line.length);
            messages = x + ", " + y +", " + line.slice(1, i) + " " + covid_list(rnd.nextInt(covid_list.length)) + " " + line.slice(i,line.length)
        } else { messages = x + ", " + y +", " + line }
        messages
    }
    var file = scala.io.Source.fromFile("dataset_smol.csv")
    var src = file.getLines.map(_.split(",")(2))

    val covid_list = Seq(
        "SARS CoV-2",
        "covid19",
        "corona", 
        "fever",
        "dry cough",
        "tiredness",
        "pains",
        "I HAVE CORONA",
        "I HAVE COVID19",
        "I am sick and am going to party and dance",
        "sore throat",
        "diarrhoea",
        "conjunctivitis",
        "headache",
        "loss of taste or smell"
    )

    val Covid_prob = 0.2
    val x_mean = 0.0
    val x_var  = 1.0
    val y_mean = 0.0
    val y_var  = 1.0
    
    val events = 10000
    val topic = "corona"
    val brokers = "localhost:9092"
    val rnd = new Random()

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
