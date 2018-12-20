package com.bdbizviz.dp.eventTrigger.process

import com.bdbizviz.dp.eventTrigger.model.EventTrigger
import com.bdbizviz.dp.kafka.producer.EventProducer
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.api.libs.ws.ning.NingWSClient

import scala.util.{Failure, Success, Try}

/*
 * @author Kartik Purohit
 */

class EventTriggerProcess {

  val logger = LoggerFactory.getLogger(classOf[EventTriggerProcess])

  def process(json : String ,
              wsClient : NingWSClient,
              outputTopic : String,
              kafkaProducer: KafkaProducer[String, String],
              eventProducer: EventProducer
             ) =  {
    logger.info(s"Processing Json ${json}")
    Try{
      Json.parse(json).validate[EventTrigger].fold(
        error =>{
          logger.error("Wrong Trigger Json")
          logger.error(error.toString())
        } ,
        eventTrigger=>
          eventTrigger match {
            case trigger: EventTrigger =>
              ComponentTriggerProcess.process(trigger, wsClient, kafkaProducer, eventProducer, outputTopic)
          }
      )
    }match {
      case Success(s) =>
      case Failure(e) =>
        logger.error("Exception while processing pipeline json")
        logger.error(e.printStackTrace().toString)
    }
  }

}
