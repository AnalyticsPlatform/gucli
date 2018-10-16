package ru.sber.cb.ap.gusli.actor.core.dto

import akka.actor.ActorRef
import akka.testkit.TestProbe
import ru.sber.cb.ap.gusli.actor.core.Entity.{EntityMetaResponse, GetEntityMeta}
import ru.sber.cb.ap.gusli.actor.core.Project.{EntityFound, FindEntity}
import ru.sber.cb.ap.gusli.actor.core.Workflow.{BindEntity, BindEntitySuccessful}
import ru.sber.cb.ap.gusli.actor.core._
import ru.sber.cb.ap.gusli.actor.core.extractor.EntityIdExtractor
import ru.sber.cb.ap.gusli.actor.core.extractor.EntityIdExtractor.EntityIdExtracted

class EntityIdExtractorSpec extends ActorBaseTest("EntityIdExtractorSpec") {
  private val receiver = TestProbe()
  private val project = TestProbe()
  private val metaDefault = WorkflowMetaDefault("wf", Map.empty)
  private val workflow: ActorRef = system.actorOf(Workflow(metaDefault, project.ref), "wf")

  "An EntityIdExtractor" when {
    "created with a workflow that binned two entities: 1,2" should {
      val entity1 = TestProbe()
      workflow ! BindEntity(1)
      project.expectMsg(FindEntity(1))
      project.reply(EntityFound(EntityMetaDefault(1, "entity1", "/entity1", None), entity1.ref))
      expectMsg(BindEntitySuccessful(1))

      val entity2 = TestProbe()
      workflow ! BindEntity(2)
      project.expectMsg(FindEntity(2))
      project.reply(EntityFound(EntityMetaDefault(2, "entity2", "/entity2", None), entity2.ref))
      expectMsg(BindEntitySuccessful(2))

      system.actorOf(EntityIdExtractor(workflow, receiver.ref))
      entity2.expectMsg(GetEntityMeta())
      entity2.reply(EntityMetaResponse(EntityMetaDefault(2, "entity2", "/entity2", None)))
      entity1.expectMsg(GetEntityMeta())
      entity1.reply(EntityMetaResponse(EntityMetaDefault(1, "entity1", "/entity1", None)))
      "send back EntityIdExtracted(Seq(1,2,3))" in {
        receiver.expectMsg(EntityIdExtracted(Set(1, 2)))
      }
    }
  }
}