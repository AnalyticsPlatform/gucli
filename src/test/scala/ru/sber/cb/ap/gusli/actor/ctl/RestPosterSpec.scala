package ru.sber.cb.ap.gusli.actor.ctl

import org.scalatest.Ignore
import play.api.libs.json.Json
import ru.sber.cb.ap.gusli.actor.core.ActorBaseTest
import ru.sber.cb.ap.gusli.actor.ctl.model.CtlJsonProtocol._
import ru.sber.cb.ap.gusli.actor.ctl.model.StatValPost

import scala.concurrent.duration._

@Ignore
class RestPosterSpec  extends ActorBaseTest("Downloader") {
  "RestPoster created with link" in {
    val statVal = StatValPost(0, 0, 0, Array("0"))
    val statValJson = Json.toJson[StatValPost](statVal)
    
    val wfId = 0
    system.actorOf(RestPoster("hide", "{}", self))
    expectMsgPF(1000000 hours) {
      case RestPoster.HttpBody(message) =>
      }
    }
}
