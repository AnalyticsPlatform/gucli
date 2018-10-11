package ru.sber.cb.ap.gusli.actor.core.clone

import akka.testkit.TestProbe
import ru.sber.cb.ap.gusli.actor.core.Category._
import ru.sber.cb.ap.gusli.actor.core._
import ru.sber.cb.ap.gusli.actor.core.clone.CategorySetCloner.CategorySetCloneSuccessfully

class CategorySetClonerSpec extends ActorBaseTest("CategoryClonerSpec") {
  private val receiverProbe = TestProbe()
  private val projectProbe: TestProbe = TestProbe()
  private val projectProbe2: TestProbe = TestProbe()
  private val cat = system.actorOf(Category(CategoryMetaDefault("category"), projectProbe.ref), "category")
  private val sub1 = system.actorOf(Category(CategoryMetaDefault("sub1"), projectProbe2.ref))
  private val sub2 = system.actorOf(Category(CategoryMetaDefault("sub2"), projectProbe2.ref))
  private val sub3 = system.actorOf(Category(CategoryMetaDefault("sub3"), projectProbe2.ref))

  "An `CategorySetCloner`" when {
    "created with subcategories set" should {
      system.actorOf(CategorySetCloner(cat, Set(sub1, sub2, sub3), receiverProbe.ref))
      "send back CategoryCloneSuccessfully()" in {
        receiverProbe.expectMsg(CategorySetCloneSuccessfully())
        cat ! GetSubcategories()
        expectMsgPF() {
          case SubcategorySet(set) =>
            for (c <- set)
              c ! GetCategoryMeta()

            expectMsgAllOf(
              CategoryMetaResponse(CategoryMetaDefault("sub1")),
              CategoryMetaResponse(CategoryMetaDefault("sub2")),
              CategoryMetaResponse(CategoryMetaDefault("sub3")))
        }
      }
    }
  }
}