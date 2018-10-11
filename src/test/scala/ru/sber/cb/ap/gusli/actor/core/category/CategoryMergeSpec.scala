package ru.sber.cb.ap.gusli.actor.core.category

import akka.actor.ActorRef
import akka.testkit.{ImplicitSender, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import ru.sber.cb.ap.gusli.actor.core.Category.{AddSubcategory, SubcategoryCreated, _}
import ru.sber.cb.ap.gusli.actor.core.Workflow.{GetWorkflowMeta, WorkflowMetaResponse}
import ru.sber.cb.ap.gusli.actor.core._

class CategoryMergeSpec extends ActorBaseTest("CategorySpec")
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  private val projectProbe: TestProbe = TestProbe()
  private val currCat = system.actorOf(Category(CategoryMetaDefault("category", Map.empty), projectProbe.ref), "curr-cat")
  private val prevCat = system.actorOf(Category(CategoryMetaDefault("category", Map.empty), projectProbe.ref), "prev-cat")
  private var cat1: ActorRef = _
  private var prevCat1: ActorRef = _
  private var cat11: ActorRef = _
  private var prevCat11: ActorRef = _
  currCat ! AddSubcategory(CategoryMetaDefault("cat-1", Map.empty))
  expectMsgPF() {
    case SubcategoryCreated(c) =>
      cat1 = c
  }
  prevCat ! AddSubcategory(CategoryMetaDefault("cat-1", Map.empty))
  expectMsgPF() {
    case SubcategoryCreated(c) =>
      prevCat1 = c
  }

  cat1 ! AddSubcategory(CategoryMetaDefault("cat-11", Map.empty))
  expectMsgPF() {
    case SubcategoryCreated(c) =>
      cat11 = c
  }

  prevCat1 ! AddSubcategory(CategoryMetaDefault("cat-11", Map.empty))
  expectMsgPF() {
    case SubcategoryCreated(c) =>
      prevCat11 = c
  }


  "An empty Category" when {
    "send GetCategoryMeta" should {
      currCat ! GetCategoryMeta()
      "send back CategoryMetaResponse(\"category\")" in {
        expectMsg(CategoryMetaResponse(CategoryMetaDefault("category", Map.empty)))
      }
    }


  }
}