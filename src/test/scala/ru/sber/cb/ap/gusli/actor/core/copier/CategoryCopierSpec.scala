package ru.sber.cb.ap.gusli.actor.core.copier

import akka.testkit.TestProbe
import ru.sber.cb.ap.gusli.actor.core.Category._
import ru.sber.cb.ap.gusli.actor.core._
import ru.sber.cb.ap.gusli.actor.core.copier.CategoryCopier.CopiedSuccessfully

class CategoryCopierSpec extends ActorBaseTest("CategoryCopierSpec") {

  private val currProjectProbe = TestProbe()
  private val prevProjectProbe = TestProbe()

  private val currMeta = CategoryMetaDefault("category", Map("p1" -> "111", "p2" -> "222"))
  private val prevMeta = CategoryMetaDefault("category", Map("p2" -> "222", "p1" -> "111"))

  private val currentCat = system.actorOf(Category(currMeta, currProjectProbe.ref))
  private val prevCat = system.actorOf(Category(prevMeta, prevProjectProbe.ref))

  private val prevSubMeta1 = CategoryMetaDefault("prevSubMeta1", Map("sub-param-1" -> "sub-val-1"))
  private val prevSubMeta2 = CategoryMetaDefault("prevSubMeta2", Map("sub-param-2" -> "sub-val-2"))

  prevCat ! AddSubcategory(prevSubMeta1)
  expectMsgClass(classOf[SubcategoryCreated])
  prevCat ! AddSubcategory(prevSubMeta2)
  expectMsgClass(classOf[SubcategoryCreated])

  private val receiverProbe = TestProbe()
  "CategoryCopier" must {
    "create all subcategories and send back CopiedSuccessfully()" in {
      system.actorOf(CategoryCopier(currProjectProbe.ref, prevProjectProbe.ref, currentCat, prevCat, receiverProbe.ref))
      receiverProbe.expectMsg(CopiedSuccessfully())
      currentCat ! GetSubcategories()
      expectMsgPF() {
        case SubcategorySet(set) =>
          assert(set.size == 2)
          for (c <- set)
            c ! GetCategoryMeta()
          expectMsgAllOf(CategoryMetaResponse(prevSubMeta1), CategoryMetaResponse(prevSubMeta2))
      }
    }
  }

}


