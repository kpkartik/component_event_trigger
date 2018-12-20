package com.bdbizviz.dp.eventTrigger.process

import com.bdbizviz.dp.entities.components.ComponentV1
import com.bdbizviz.dp.eventTrigger.model.EventTrigger
import com.bdbizviz.dp.eventTrigger.util.MDSUtil
import com.bdbizviz.dp.kafka.producer.EventProducer
import org.apache.kafka.clients.producer.KafkaProducer
import play.api.libs.ws.ning.NingWSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/*
 * @author Kartik Purohit
 */

object ComponentTriggerProcess {

  def process(eventTrigger: EventTrigger,
              wsClient : NingWSClient,
              kafkaProducer: KafkaProducer[String, String],
              eventProducer: EventProducer,
              outputTopic : String) = {

    MDSUtil.getPipeline(eventTrigger.pipelineId, wsClient).map{
      case Success(pipelineV1) =>
        pipelineV1.components.foreach{
          case componentV1 : ComponentV1 =>
            if(componentV1.invocationMethodType.equalsIgnoreCase("batch") ){
              if(eventTrigger.eventTopics.length > 0 && eventTrigger.eventTopics.contains(componentV1.inEvent)) {
                ComponentProcess.process(componentV1.copy(action = "start", offsetStart = Some(eventTrigger.startOffset), offsetEnd = Some(eventTrigger.endOffset)),
                  outputTopic, kafkaProducer,eventProducer, wsClient, eventTrigger.pipelineId)
              }
              if(componentV1.componentInstanceId.get.equalsIgnoreCase(eventTrigger.componentInstId)) {
                ComponentProcess.process(componentV1.copy(action = "stop"), outputTopic, kafkaProducer,eventProducer, wsClient, eventTrigger.pipelineId)
              }
            }
        }
      case Failure(f) =>
        println(f)
    }
  }

}
