package ru.sber.cb.ap.gusli.actor.ctl

import org.scalatest.Ignore
import play.api.libs.json.Json
import ru.sber.cb.ap.gusli.actor.core.ActorBaseTest
import ru.sber.cb.ap.gusli.actor.ctl.model.CtlJsonProtocol._
import ru.sber.cb.ap.gusli.actor.ctl.model.Entity

import scala.concurrent.duration._

@Ignore
class RestGetterSpec  extends ActorBaseTest("Downloader") {
  "RestGetter created with entity-get link" should {
    "send back entity json" in {
      val link = "hide"
      system.actorOf(RestGetter(link, self))
      val expectedEntity = Entity("ap/rb/wut", None, 0, "HDFS", 0)
  
      expectMsgPF(5 seconds) { case RestGetter.HttpBody(message) =>
        val jsonEntity = Json.parse(message)
        val receivingEntity = Json.fromJson[Entity](jsonEntity).get
        println(Console.GREEN + jsonEntity + Console.WHITE)
        assert(receivingEntity == expectedEntity)
      }
    }
    "send back 10 responses" when {
      "creating 10 instances" in {
        val link = "hide"
        (1 to 10).foreach(_ => system.actorOf(RestGetter(link, self)))
        (1 to 10).foreach(_ => expectMsgClass(15 seconds ,classOf[RestGetter.HttpBody]))
      }
    }
  }
}
