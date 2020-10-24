package sparkstreaming

import java.util.HashMap
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka._
import kafka.serializer.{DefaultDecoder, StringDecoder, Decoder}
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.storage.StorageLevel
import java.util.{Date, Properties}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, ProducerConfig}
import scala.util.Random
import scala.math.{sqrt, pow}

import org.apache.spark.sql.cassandra._
import com.datastax.spark.connector._
import com.datastax.driver.core.{Session, Cluster, Host, Metadata}
import com.datastax.spark.connector.streaming._

object KafkaSpark {
  def main(args: Array[String]) {
    // connect to Cassandra and make a keyspace and table as explained in the document
    val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
    val session = cluster.connect()
      
    session.execute("CREATE KEYSPACE IF NOT EXISTS covid WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor': 1 };")
    session.execute("CREATE TABLE IF NOT EXISTS covid.cell_counts (cell float PRIMARY KEY, count float);")
    println("CASSANDRA")

    // make a connection to Kafka and read (key, value) pairs from it
    val kafkaConf = Map("metadata.broker.list" -> "localhost:9092",
                        "zookeeper.connect" -> "localhost:2181",
                        "group.id" -> "kafka-spark-streaming",
                        "zookeeper.connection.timeout.ms" -> "1000")
    
    val sc = new SparkConf().setAppName("SparkStreaming").setMaster("local")
    val ssc = new StreamingContext(sc, Seconds(10))
    ssc.checkpoint(".")

    val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaConf, Set("covid"))
    val variable_name = messages.map(_._2)
    val pairs = variable_name.map(x => {
      val split = x.split(", ")
      ((split(0).toDouble, split(1).toDouble), split.drop(2).mkString(""))
    })
    pairs.print()

    val getIndex = (p: (Double, Double)) => {
      val GRID_RES = 10
      val GRID_CELLS = GRID_RES * GRID_RES
      val STEP_X = 3480 / GRID_RES
      val STEP_Y = 2470/ GRID_RES
      val indices = 0 to GRID_CELLS
      indices.minBy(index => {
          val x = index % GRID_RES * STEP_X + STEP_X / 2
          val y = (index / GRID_RES).floor * STEP_Y + STEP_Y / 2
          sqrt(pow(x - p._1, 2) + pow(y - p._2, 2))
      })
    }
    val index_messsage = pairs.map(p => (getIndex(p._1), p._2))
    index_messsage.print() 

    // measure the average value for each key in a stateful manner
    // val mappingFunc = (key: String, value: Option[Double], state: State[Double]) => {
    //   val prevAvg = state.getOption.getOrElse(0.0)
    //   val N = 10
    //   val avg = prevAvg - (prevAvg / N) + (value.getOrElse(0.0) / N)
    //   state.update(avg)
    //   (key, avg)
    // }
    // val stateDstream = pairs.mapWithState(StateSpec.function(mappingFunc))
    // store the result in Cassandra
    // stateDstream.saveToCassandra("avg_space", "avg")

    ssc.start()
    ssc.awaitTermination()
  }
}

