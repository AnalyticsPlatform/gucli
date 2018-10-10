package ru.sber.cb.ap.gusli.actor.core.ctl

import ru.sber.cb.ap.gusli.actor.core.ActorBaseTest
import ru.sber.cb.ap.gusli.actor.ctl.RestGetter
import ru.sber.cb.ap.gusli.actor.ctl.RestGetter.Response

import concurrent.duration._

class RestGetterSpec extends ActorBaseTest("Downloader") {
  "Myself for preStart with url" must {
    "print text" in {
      system.actorOf(RestGetter("https://httpbin.org/get", self))
      expectMsgAnyClassOf(classOf[Response])
    }
  }
}
