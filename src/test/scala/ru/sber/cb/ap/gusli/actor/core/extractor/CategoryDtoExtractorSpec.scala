package ru.sber.cb.ap.gusli.actor.core.extractor

import akka.actor.ActorRef
import akka.testkit.TestProbe
import ru.sber.cb.ap.gusli.actor.core.Category.{AddSubcategory, CreateWorkflow, SubcategoryCreated, WorkflowCreated}
import ru.sber.cb.ap.gusli.actor.core._
import ru.sber.cb.ap.gusli.actor.core.extractor.CategoryDtoExtractor.CategoryDtoExtracted

class CategoryDtoExtractorSpec extends ActorBaseTest("CategoryDtoExtractorSpec") {
  private val projectProbe: TestProbe = TestProbe()
  private val cat = system.actorOf(Category(CategoryMetaDefault("category", Map.empty), projectProbe.ref), "category")
  private val receiverProbe = TestProbe()
  private var cat1: ActorRef = _
  private var cat2: ActorRef = _

  cat ! AddSubcategory(CategoryMetaDefault("cat-1", Map.empty))
  expectMsgPF() {
    case SubcategoryCreated(c) =>
      cat1 = c
  }
  cat ! AddSubcategory(CategoryMetaDefault("cat-2", Map("tableA" -> "tableB"), params = Map("c2" -> "2")))
  expectMsgPF() {
    case SubcategoryCreated(c) =>
      cat2 = c
  }

  cat2 ! CreateWorkflow(WorkflowMetaDefault("wf-2", Map("sql" -> "select 1 as w1")))
  expectMsgPF() {
    case WorkflowCreated(_) =>
  }

  cat2 ! AddSubcategory(CategoryMetaDefault("cat-22", Map("table22" -> "tableCC")))
  expectMsgAnyClassOf(classOf[SubcategoryCreated])

  "An SubcategoryDtoExtractor" when {
    "created" should {
      system.actorOf(CategoryDtoExtractor(cat, receiverProbe.ref))
      "return CategoryExtracted with dto" in {
        receiverProbe.expectMsgPF() {
          case CategoryDtoExtracted(dto) =>
            assert(dto.name == "category")
            assert(dto.subcategories.size == 2)
            dto.subcategories.find(_.name == "cat-2") match {
              case Some(cat2Dto) =>
                assert(cat2Dto.name == "cat-2")
                assert(cat2Dto.sqlMap == Map("tableA" -> "tableB"))
                assert(cat2Dto.params == Map("c2" -> "2"))

                val wf2Dto = cat2Dto.workflows.head
                assert(wf2Dto.name == "wf-2")
                assert(wf2Dto.sql == Map("sql" -> "select 1 as w1"))


                val cat22Dto=cat2Dto.subcategories.head
                assert(cat22Dto.name == "cat-22")
                assert(cat22Dto.sqlMap == Map("table22" -> "tableCC"))
            }
        }
      }
    }
  }
}