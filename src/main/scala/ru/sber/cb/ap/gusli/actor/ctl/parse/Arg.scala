package ru.sber.cb.ap.gusli.actor.ctl.parse

import com.fasterxml.jackson.core.JsonParseException
import play.api.libs.json.{JsArray, Json}

object Arg {
  def apply(arg: String): Set[String] = {
    tryParseArg(arg)
  }
  
  private def tryParseArg(arg: String) = {
    try
      parseJsArray(arg)
    catch {
      case e: JsonParseException => Set(arg)
    }
  }
  
  private def parseJsArray(arg: String) = {
    Json.parse(arg).as[JsArray].value.map(a => a.as[String]).toSet
  }
}
