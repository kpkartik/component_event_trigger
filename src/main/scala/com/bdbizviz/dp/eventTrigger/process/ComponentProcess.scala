package com.bdbizviz.dp.eventTrigger.process

import com.bdbizviz.dp.entities.components.ComponentV1
import com.bdbizviz.dp.eventTrigger.util.MDSUtil
import com.bdbizviz.dp.kafka.producer.EventProducer
import org.apache.kafka.clients.producer.KafkaProducer
import play.api.libs.json.Json
import play.api.libs.ws.ning.NingWSClient

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/*
 * @author Kartik Purohit
 */
object ComponentProcess {

  def process(
               componentV1: ComponentV1,
               outputTopic  : String,
               kafkaProducer: KafkaProducer[String, String],
               eventProducer: EventProducer ,
               wsClient :NingWSClient ,
               pipelineId : String
             ) = {

    MDSUtil.getComponentV1Instance(wsClient , componentV1.componentInstanceId.get).map{
      case Success(componentV1Instance) =>
        if(!componentV1.action.equalsIgnoreCase(componentV1Instance.metaData.action)){
          componentV1.action.toLowerCase match {
            case "start" =>
              MDSUtil.addComponentV1Instance(componentV1 , pipelineId , wsClient).map{
                case Success(s) =>
                  eventProducer.sendMessage(kafkaProducer , Json.toJson(s.metaData).toString() , outputTopic)
                case Failure(e) =>
                  e.printStackTrace()
              }
            case "stop" =>
              MDSUtil.updateComponentV1Instance(
                componentV1Instance.copy(
                  metaData = componentV1.copy(displayName = componentV1.displayName + "-" +componentV1Instance.uuid) ,
                  status = "Stopping"
                ) ,
                pipelineId ,
                wsClient
              ).map{
                case Success(s) =>
                  eventProducer.sendMessage(kafkaProducer , Json.toJson(s.metaData).toString() , outputTopic)
                case Failure(e) =>
                  e.printStackTrace()
              }
            case "refresh" =>
              MDSUtil.updateComponentV1Instance(
                componentV1Instance.copy(
                  metaData = componentV1.copy(
                    displayName = componentV1.displayName + "-" +componentV1Instance.uuid ,
                    action = "stop"
                  ) ,
                  status = "Stopping"
                ) ,
                pipelineId ,
                wsClient
              ).map{
                case Success(s) =>
                  eventProducer.sendMessage(kafkaProducer , Json.toJson(s.metaData).toString() , outputTopic)
                case Failure(e) =>
                  e.printStackTrace()
              }
              MDSUtil.addComponentV1Instance(componentV1 , pipelineId , wsClient).map{
                case Success(s) =>
                  eventProducer.sendMessage(kafkaProducer , Json.toJson(s.metaData).toString() , outputTopic)
                case Failure(e) =>
                  e.printStackTrace()
              }
              MDSUtil.updatePipelineV1ComponentInstance(pipelineId , componentV1 , wsClient).map{
                f =>
                  if(f) println("Pipeline Updated") else print("Pipeline Update Failed.")
              }
          }
        }else{
          println("No Doing Anything Same action.")
        }
      case Failure(e) =>
        if(e.getMessage.equalsIgnoreCase("no component instance found.")){
          if(componentV1.action.equalsIgnoreCase("start")){
            MDSUtil.addComponentV1Instance(componentV1 , pipelineId , wsClient).map{
              case Success(s) =>
                eventProducer.sendMessage(kafkaProducer , Json.toJson(s.metaData).toString() , outputTopic)
              case Failure(e) =>
                e.printStackTrace()
            }
          }
        }else{
          e.printStackTrace()
        }


    }

  }

}
