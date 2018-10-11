package ru.sber.cb.ap.gusli.actor.core.clone

import akka.actor.ActorRef
import akka.testkit.TestProbe
import ru.sber.cb.ap.gusli.actor.core.Category.{AddSubcategory, SubcategoryCreated, _}
import ru.sber.cb.ap.gusli.actor.core._
import ru.sber.cb.ap.gusli.actor.core.clone.CategoryCloner.CategoryCloneSuccessfully

class CategoryClonerForSubsWithoutChildrenSpec extends ActorBaseTest("CategoryClonerForSubsWithoutChildrenSpec") {
  private val receiverProbe = TestProbe()
  private val projectProbe: TestProbe = TestProbe()
  private val cat = system.actorOf(Category(CategoryMetaDefault("category"), projectProbe.ref), "category")
  private val sub = system.actorOf(Category(CategoryMetaDefault("sub"), projectProbe.ref), "sub")


  "An `CategoryCloner`" when {
    "created with subcategories without children" should {
      system.actorOf(CategoryCloner(cat, sub, receiverProbe.ref))
      "send back CategoryCloneSuccessfully()" in {
        receiverProbe.expectMsg(CategoryCloneSuccessfully())
        cat ! GetSubcategories()
        expectMsgPF(){
          case SubcategorySet(s)=>
            val sub = s.head
            sub ! GetCategoryMeta()
            expectMsg(CategoryMetaResponse(CategoryMetaDefault("sub")))

            sub ! GetSubcategories()
            expectMsgPF(){
              case SubcategorySet(ss)=>
                assert(ss.isEmpty)
            }
        }
      }
    }
  }
}