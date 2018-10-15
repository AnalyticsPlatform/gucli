package ru.sber.cb.ap.gusli.actor.core.extractor

import akka.testkit.TestProbe
import ru.sber.cb.ap.gusli.actor.core._
import ru.sber.cb.ap.gusli.actor.core.dto.{CategoryDto, WorkflowDto}
import ru.sber.cb.ap.gusli.actor.core.extractor.EntityIdFromCategoryDtoExtractor.EntityIdFromCategoryDtoExtracted

class EntityIdFromCategoryDtoExtractorSpec extends ActorBaseTest("EntityIdFromCategoryDtoExtractorSpec") {
  private val receiverProbe = TestProbe()
  private val wf111 = WorkflowDto(WorkflowMetaDefault("wf-111", Map.empty), entities = Set(111L, 112L))
  private val subCat11 = CategoryDto(CategoryMetaDefault("subcat-11", entities = Set(11L, 12L)), Set.empty[CategoryDto], Set(wf111))
  private val wf11 = WorkflowDto(WorkflowMetaDefault("wf-11", Map.empty), entities = Set(13L, 14L))
  private val subCat1 = CategoryDto(CategoryMetaDefault("subcat-1"), subcategories = Set(subCat11), workflows = Set(wf11))
  private val cat = CategoryDto(CategoryMetaDefault("category", entities = Set(1L)), subcategories = Set(subCat1))

  "An EntityIdFromCategoryDtoExtractor" when {
    "created" should {
      system.actorOf(EntityIdFromCategoryDtoExtractor(cat, receiverProbe.ref))
      "return EntityIdFromCategoryDtoExtracted with category's and workflow's entity ids" in {
        receiverProbe.expectMsg(EntityIdFromCategoryDtoExtracted(Set(1L, 11L, 12L, 13L, 14L, 111L, 112L)))

        receiverProbe.expectNoMessage()
      }
    }
  }
}