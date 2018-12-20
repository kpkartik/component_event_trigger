package com.bdbizviz.dp.eventTrigger.model

import play.api.libs.json.Json

/*
 * @author Kartik Purohit
 */

case class EventTrigger (pipelineId : String,
                         eventTopics : Array[String],
                         startOffset : Map[String, Long],
                         endOffset : Map[String, Long],
                         componentInstId : String
                   )

object EventTrigger {
  implicit lazy val eventTrigger = Json.format[EventTrigger]
}
