package ru.sber.cb.ap.gusli.actor.core.clone

import akka.actor.ActorRef
import akka.testkit.TestProbe
import ru.sber.cb.ap.gusli.actor.core.Category.{AddSubcategory, SubcategoryCreated, _}
import ru.sber.cb.ap.gusli.actor.core._
import ru.sber.cb.ap.gusli.actor.core.clone.CategoryCloner.CategoryCloneSuccessfully

class CategoryClonerSpec extends ActorBaseTest("CategoryClonerSpec") {
  private val receiverProbe = TestProbe()
  private val projectProbe: TestProbe = TestProbe()
  private val cat = system.actorOf(Category(CategoryMetaDefault("category"), projectProbe.ref), "category")
  private val sub = system.actorOf(Category(CategoryMetaDefault("sub"), projectProbe.ref), "sub")
  private var cat1: ActorRef = _
  private var cat11: ActorRef = _
  private var cat111: ActorRef = _

  sub ! AddSubcategory(CategoryMetaDefault("cat-1"))
  expectMsgPF() {
    case SubcategoryCreated(c) =>
      cat1 = c
  }

  cat1 ! AddSubcategory(CategoryMetaDefault("cat-11"))
  expectMsgPF() {
    case SubcategoryCreated(c) =>
      cat11 = c
  }

  cat11 ! AddSubcategory(CategoryMetaDefault("cat-111"))
  expectMsgPF() {
    case SubcategoryCreated(c) =>
      cat111 = c
  }


  "An `CategoryCloner`" when {
    "created with subcategories to copy" should {
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
                val c1 = ss.head
                c1 ! GetCategoryMeta()
                expectMsg(CategoryMetaResponse(CategoryMetaDefault("cat-1")))

                c1 ! GetSubcategories()
                expectMsgPF(){
                  case SubcategorySet(sss)=>
                    val c11 = sss.head
                    c11 ! GetCategoryMeta()
                    expectMsg(CategoryMetaResponse(CategoryMetaDefault("cat-11")))

                    c11 ! GetSubcategories()
                    expectMsgPF(){
                      case SubcategorySet(ssss)=>
                        val c111 = ssss.head
                        c111 ! GetCategoryMeta()
                        expectMsg(CategoryMetaResponse(CategoryMetaDefault("cat-111")))
                    }
                }
            }
        }
      }
    }
  }
}