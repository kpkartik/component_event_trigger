package com.bdbizviz.dp.eventTrigger

import java.util.Properties

import com.bdbizviz.dp.eventTrigger.process.EventTriggerProcess
import com.bdbizviz.dp.kafka.config.KafkaConfig
import com.bdbizviz.dp.kafka.consumer.EventConsumer
import com.bdbizviz.dp.kafka.producer.EventProducer
import com.bdbizviz.dp.kafka.utils.KafkaUtils
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.ForeachAction
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import play.api.libs.ws.ning.NingWSClient

/*
 * @author Kartik Purohit
 */

object Executor  {

  def main(args:Array[String]):Unit ={
    val stream = new EventConsumer().
      getStreamingConsumer[String , String](topic = sys.env("EVENT_TRIGGER_TOPIC"))

    val eventTriggerProcess = new EventTriggerProcess()

    val kafkaProducer = KafkaUtils.getProducerInstance
    val eventProducer = new EventProducer
    val wsClient = NingWSClient()

    stream._1.foreach(new ForeachAction[String , String] {
      override def apply(key: String, value: String):Unit = {
        println(s"${key -> value}")
        eventTriggerProcess.process(value , wsClient, sys.env("TSK_OUT_TOPIC"),  kafkaProducer, eventProducer)
      }
    })

    val config :Properties =KafkaConfig.getConfig()
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass)
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass)
    val streams: KafkaStreams = new KafkaStreams(stream._2.build() , config)
    streams.start()
  }

}



