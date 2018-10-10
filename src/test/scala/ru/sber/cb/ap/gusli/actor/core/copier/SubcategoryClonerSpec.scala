package ru.sber.cb.ap.gusli.actor.core.copier

import akka.actor.ActorRef
import akka.testkit.TestProbe
import ru.sber.cb.ap.gusli.actor.core.Category._
import ru.sber.cb.ap.gusli.actor.core._
import ru.sber.cb.ap.gusli.actor.core.copier.SubcategoryCloner.SubcategoryCloneSuccessful

class SubcategoryClonerSpec extends ActorBaseTest("SubcategoryClonerSpec") {

  private val currProjectProbe = TestProbe()
  private val prevProjectProbe = TestProbe()
  private val receiverProbe = TestProbe()
  private val currMeta = CategoryMetaDefault("category", Map("p1" -> "111", "p2" -> "222"))
  private val prevMeta = CategoryMetaDefault("category", Map("p2" -> "222", "p1" -> "111"))
  private val prevSubMeta = CategoryMetaDefault("prevSubMeta", Map("subParams" -> "subVal"))
  private val currentCat = system.actorOf(Category(currMeta, currProjectProbe.ref))
  private val prevCat = system.actorOf(Category(prevMeta, prevProjectProbe.ref))

  private var subCat: ActorRef = _

  prevCat ! AddSubcategory(prevSubMeta)
  expectMsgPF() {
    case SubcategoryCreated(c) =>
      subCat = c
  }
  "SubcategoryCloner" must {
    "create Subcategory and send back SubcategoryCloneSuccessful(...)" in {
      system.actorOf(SubcategoryCloner(currProjectProbe.ref, prevProjectProbe.ref, currentCat, subCat, receiverProbe.ref))
      receiverProbe.expectMsgPF() {
        case SubcategoryCloneSuccessful(clonedCategory, fromCategory) =>
          assert(fromCategory == subCat)
          clonedCategory ! GetCategoryMeta()
          expectMsg(CategoryMetaResponse(prevSubMeta))
      }
    }
  }

}


