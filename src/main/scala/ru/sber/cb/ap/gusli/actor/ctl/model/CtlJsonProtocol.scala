package ru.sber.cb.ap.gusli.actor.ctl.model

import play.api.libs.json.Json

/**
  * Contains implcits for using Json.fromJson[T]. It's important to write vals in certain order. As example:
  * WfExt = WfExt(Wf), so Wf should be before WfExt. Another example: Wf = Wf(Param)
  */
object CtlJsonProtocol {
  implicit val entityReads = Json.reads[Entity]
  implicit val statValReads = Json.reads[StatValPost]
  implicit val paramReads = Json.reads[Param]
  implicit val catReads = Json.reads[Category]
  implicit val wfReads = Json.reads[Wf]
  implicit val wfExtReads = Json.reads[WfExt]
  implicit val entityWrites = Json.writes[Entity]
  implicit val statValWrites = Json.writes[StatValPost]
  implicit val paramWrites = Json.writes[Param]
  implicit val catWrites = Json.writes[Category]
  implicit val wfWrites = Json.writes[Wf]
  implicit val wfExtWrites = Json.writes[WfExt]
}
