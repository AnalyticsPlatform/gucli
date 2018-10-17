package ru.sber.cb.ap.gusli.actor.core.extractor

import akka.actor.ActorRef
import akka.testkit.TestProbe
import ru.sber.cb.ap.gusli.actor.core.Category.{AddSubcategory, CreateWorkflow, SubcategoryCreated, WorkflowCreated}
import ru.sber.cb.ap.gusli.actor.core.Entity._
import ru.sber.cb.ap.gusli.actor.core._
import ru.sber.cb.ap.gusli.actor.core.dto.EntityDto
import ru.sber.cb.ap.gusli.actor.core.extractor.CategoryDtoExtractor.CategoryDtoExtracted
import ru.sber.cb.ap.gusli.actor.core.extractor.EntityDtoExtractor.EntityDtoExtracted

class EntityDtoExtractorSpec extends ActorBaseTest("EntityDtoExtractorSpec") {
  private val receiver = TestProbe()
  private val rootMeta = EntityMetaDefault(0, "category", "category", None)
  private val root = system.actorOf(Entity(rootMeta))
  root ! AddChildEntity(EntityMetaDefault(1, "cat-1", "category/cat-1", Some(0)))
  expectMsgClass(classOf[EntityCreated])
  root ! AddChildEntity(EntityMetaDefault(2, "cat-2", "category/cat-2", Some(0)))
  expectMsgPF() {
    case EntityCreated(cat2) =>
      cat2 ! AddChildEntity(EntityMetaDefault(21, "cat-21", "category/cat-2/", Some(0)))
  }
  expectMsgClass(classOf[EntityCreated])


  "An EntityDtoExtractor" when {
    "created to extract root entity" should {
      system.actorOf(EntityDtoExtractor(root, receiver.ref))
      "EntityDtoExtractor should be sent back" in {
        receiver.expectMsg(EntityDtoExtracted(
          EntityDto(EntityMetaDefault(0, "category", "category", None),
            Set(
              EntityDto(EntityMetaDefault(1, "cat-1", "category/cat-1", Some(0)), Set.empty),
              EntityDto(EntityMetaDefault(2, "cat-2", "category/cat-2", Some(0)), Set(EntityDto(EntityMetaDefault(21, "cat-21", "category/cat-2/", Some(0)), Set.empty))
              )))))
      }
    }
  }
}