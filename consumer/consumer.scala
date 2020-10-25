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

    // make a connection to Kafka and read (key, value) pairs from it
    val kafkaConf = Map("metadata.broker.list" -> "localhost:9092",
                        "zookeeper.connect" -> "localhost:2181",
                        "group.id" -> "kafka-spark-streaming",
                        "zookeeper.connection.timeout.ms" -> "1000")
      
    val covid_list = scala.io.Source.fromFile("covidlist.csv").getLines.toList
    
    val sc = new SparkConf().setAppName("SparkStreaming").setMaster("local")
    val ssc = new StreamingContext(sc, Seconds(10))
    ssc.checkpoint(".")

    val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaConf, Set("covid"))
    val variable_name = messages.map(_._2)
    val pairs = variable_name
    .map(x => {
      val split = x.split(", ")
      ((split(0).toDouble, split(1).toDouble), split.drop(2).mkString(""))
    })
    .filter(p => covid_list.exists(p._2.contains))
    
    val getIndex = (p: (Double, Double)) => {
      val GRID_RES = 100
      val GRID_CELLS = GRID_RES * GRID_RES
      val STEP_X = 3480 / GRID_RES
      val STEP_Y = 2470/ GRID_RES
      val indices = 0 to GRID_CELLS
      indices.minBy(index => {
          val x = (index % GRID_RES) * STEP_X + STEP_X / 2
          val y = (index / GRID_RES).floor * STEP_Y + STEP_Y / 2
          sqrt(pow(x - p._1, 2) + pow(y - p._2, 2))
      })
    }
    val indexes = pairs.map(p => (getIndex(p._1), 1))

    // keep count for each index in stateful manner
    val mappingFunc = (key: Int, value: Option[Int], state: State[Map[Int, Int]]) => {
      val prevState = state.getOption.getOrElse(Map[Int, Int]().withDefaultValue(0))
      val count = prevState(key) + 1
      val newState = prevState + (key -> count)
      state.update(newState)
      (key, count)
    }
    val stateDstream = indexes.mapWithState(StateSpec.function(mappingFunc))
    stateDstream.print()

    // store the result in Cassandra
    stateDstream.saveToCassandra("covid", "cell_counts")

    ssc.start()
    ssc.awaitTermination()
  }
}

