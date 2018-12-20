package com.bdbizviz.dp.eventTrigger.util

import java.util.concurrent.ThreadLocalRandom

/*
 * @author Kartik Purohit
 */
object Util {

  def uuidGenerator() : String = {
    val arr : String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    val id : String = arr.charAt(ThreadLocalRandom.current().nextInt(0, 61)).toString +
      arr.charAt(ThreadLocalRandom.current().nextInt(0, 61)).toString+
      arr.charAt(ThreadLocalRandom.current().nextInt(0, 61)).toString+
      arr.charAt(ThreadLocalRandom.current().nextInt(0, 61)).toString
    id
  }

}
