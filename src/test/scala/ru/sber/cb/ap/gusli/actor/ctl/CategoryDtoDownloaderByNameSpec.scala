package ru.sber.cb.ap.gusli.actor.ctl

import org.scalatest.Ignore
import ru.sber.cb.ap.gusli.actor.core.ActorBaseTest
import ru.sber.cb.ap.gusli.actor.ctl.api.{CtlToProjectConfigWithCategoryNames, EnvTypes}
import ru.sber.cb.ap.gusli.actor.ctl.category.CategoryDtoDownloaderByName
import ru.sber.cb.ap.gusli.actor.ctl.category.CategoryDtoDownloaderByName.CategoryDtoResponse

import scala.concurrent.duration._

@Ignore
class CategoryDtoDownloaderByNameSpec extends ActorBaseTest("CategoryDtoDownloaderByName"){
  "CategoryDtoDownloaderByName getting url" should {
    val link = "hide"
    val ctlConfig = CtlToProjectConfigWithCategoryNames(EnvTypes.Dev, Set("cb/ap/rb", "cb/ap/rb/dev"))
    "send Map(catName -> Set[WfDto])" in {
      system.actorOf(CategoryDtoDownloaderByName(ctlConfig, self))
      expectMsgPF() {
        case CategoryDtoResponse(m) =>
          for ((c, w) <- m) {
            println(c, w)
            println( Console.YELLOW + s"There are ${w.size} workflows in ${c}" + Console.WHITE)
            w.foreach{wf =>
              println(wf.name)
            }
          }
      }
    }
    // TODO test invalid URL
  }
}
