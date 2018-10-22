package ru.sber.cb.ap.gusli.actor.core.ctl

import org.scalatest.Ignore
import ru.sber.cb.ap.gusli.actor.core.ActorBaseTest
import ru.sber.cb.ap.gusli.actor.ctl.RestGetter
import ru.sber.cb.ap.gusli.actor.ctl.RestGetter.HttpBody

import concurrent.duration._

@Ignore
class RestGetterSpec extends ActorBaseTest("Downloader") {
  "Myself for preStart with url" must {
    "print text" in {
      system.actorOf(RestGetter("https://httpbin.org/get", self))
      expectMsgAnyClassOf(classOf[HttpBody])
    }
  }
}
