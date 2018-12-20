package com.bdbizviz.dp.eventTrigger.util

import com.bdbizviz.dp.entities.components.{ComponentV1, ComponentV1Instance}
import com.bdbizviz.dp.entities.pipeline.PipelineV1
import play.api.libs.json.{JsString, Json}
import play.api.libs.ws.ning.NingWSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/*
 * @author Kartik Purohit
 */

object MDSUtil {

  def getPipeline(pipelineId : String , wsClient : NingWSClient) : Future[Try[PipelineV1]] = {
    wsClient.url(sys.env("MDS_BASE_URL") + s"/api/v1/pipeline/getById/${pipelineId}").get().map{
      f =>
        Try(
          if(Json.parse(f.body).\("success").as[Boolean]){
            Json.parse(f.body).\("data").as[PipelineV1]
          }else{
              throw new Exception("Internal Server Error")
          }
        )
    }
  }

  def getComponentV1InstMetaData(componentInstanceId:String , wsClient : NingWSClient) : Try[ComponentV1] = {
    val response = Json.parse(
      scala.io.Source.fromURL(
        sys.env("MDS_BASE_URL") + s"/api/v1/componentInst/metadata/${componentInstanceId}"
      ).mkString
    )
    Try{
      response.\("success").getOrElse(JsString("false")).as[Boolean] match {
        case true =>
          response.\("data").get.as[ComponentV1]
        case false =>
          throw new Exception(s"No Component fount bt $componentInstanceId")
      }
    }
  }

  def addComponentV1Instance (component: ComponentV1 ,pipelineId : String ,  wsClient : NingWSClient): Future[Try[ComponentV1Instance]] = {
    val uuid = Util.uuidGenerator
    val data = ComponentV1Instance(
      componentInstId = component.componentInstanceId.get ,
      pipelineId = pipelineId ,
      status = "Starting" ,
      metaData = component.copy(displayName = component.displayName + "-" +uuid).copy(action = "start") ,
      uuid = uuid ,
      isActive = true
    )
    val addURL = sys.env("MDS_BASE_URL") + "/api/v1/componentInst"
    wsClient.url(addURL).post(Json.toJson(data))
      .map { f =>
        val responseObj = Json.parse(f.body)
        Try{
          if(responseObj.\("success").get.as[Boolean]){
            data
          }else{
            throw new Exception ("Not Able to Add the Component")
          }
        }
      }
  }

  def updateComponentV1Instance (componentV1Ins: ComponentV1Instance ,pipelineId : String ,  wsClient : NingWSClient) : Future[Try[ComponentV1Instance]] = {
    wsClient.url(sys.env("MDS_BASE_URL") + "/api/v1/componentInst").put(Json.toJson(componentV1Ins))
      .map{
        f =>
          Try(Json.parse(f.body).\("data").as[ComponentV1Instance])
      }
  }

  def getComponentV1Instance(wSClient: NingWSClient , componentInsId : String): Future[Try[ComponentV1Instance]] = {
    wSClient.url(sys.env("MDS_BASE_URL") + s"/api/v1/componentInst/${componentInsId}").get().map{
      f =>
        Try(
          if(Json.parse(f.body).\("success").as[Boolean]){
            Json.parse(f.body).\("data").as[ComponentV1Instance]
          }else{
            if(Json.parse(f.body).\("errorMessage").as[String].toLowerCase.contains("no component instance found.")){
              throw new Exception("No Component Instance found.")
            }else{
              throw new Exception("Internal Server Error")
            }
          }
        )
    }
  }


  def updatePipelineV1ComponentInstance( pipelineId : String , componentV1: ComponentV1 , wSClient: NingWSClient)  = {
    wSClient.url(sys.env("MDS_BASE_URL") + s"/api/v1/pipeline/component/${pipelineId}").put(Json.toJson(componentV1.copy(action = "start"))).map{
      f =>
        Try(
          if(Json.parse(f.body).\("success").as[Boolean]){
            true
          }else{
            if(Json.parse(f.body).\("errorMessage").as[String].toLowerCase.contains("Pipeline updated failed")){
              throw new Exception("Pipeline updated failed.")
            }else{
              throw new Exception("Internal Server Error")
            }
          }
        ) match {
          case Success(s) =>
            true
          case Failure(e) =>
            false
        }
    }
  }

}
