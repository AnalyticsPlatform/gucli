package ru.sber.cb.ap.gusli.actor.core.diff.workflow

import akka.testkit.TestProbe
import ru.sber.cb.ap.gusli.actor.core.Entity.{EntityMetaResponse, GetEntityMeta}
import ru.sber.cb.ap.gusli.actor.core.Project.{EntityFound, FindEntity}
import ru.sber.cb.ap.gusli.actor.core.Workflow.BindEntity
import ru.sber.cb.ap.gusli.actor.core.diff.WorkflowSetDiffer
import ru.sber.cb.ap.gusli.actor.core.diff.WorkflowSetDiffer.WorkflowSetEquals
import ru.sber.cb.ap.gusli.actor.core.{ActorBaseTest, EntityMetaDefault, Workflow, WorkflowMetaDefault}

class WorkflowSetDifferForEqualsSpec extends ActorBaseTest("WorkflowSetDifferForEqualsSpec") {
  val receiver = TestProbe()
  private val projectProbe = TestProbe()
  private val meta1 = WorkflowMetaDefault("wf-1", Map("file1" -> "select 1"), Map("a" -> "111", "b" -> "222"))
  private val meta2 = WorkflowMetaDefault("wf-2", Map("file2" -> "select 2"), Map("c" -> "cc", "d" -> "dd"))
  private val wf1 = system.actorOf(Workflow(meta1, projectProbe.ref), "wf-1")
  private val wf1Copy = system.actorOf(Workflow(meta1, projectProbe.ref), "wf-1-copy")
  private val wf2 = system.actorOf(Workflow(meta2, projectProbe.ref), "wf-2")
  private val wf2Copy = system.actorOf(Workflow(meta2, projectProbe.ref), "wf-2-copy")
  private val eMeta1 = EntityMetaDefault(1, "e1", "/e1", None)
  private val eMeta2 = EntityMetaDefault(2, "e2", "/e2", None)
  private val e1 = TestProbe()
  private val e2 = TestProbe()

  "Two workflows with same meta and same entities must be equals" in {
    wf1 ! BindEntity(1)
    wf1Copy ! BindEntity(1)
    for (_ <- 1 to 2) {
      projectProbe.expectMsg(FindEntity(1))
      projectProbe.reply(EntityFound(eMeta1, e1.ref))
    }

    wf1 ! BindEntity(2)
    wf1Copy ! BindEntity(2)
    for (_ <- 1 to 2) {
      projectProbe.expectMsg(FindEntity(2))
      projectProbe.reply(EntityFound(eMeta2, e2.ref))
    }

    wf2 ! BindEntity(2)
    wf2Copy ! BindEntity(2)
    for (_ <- 1 to 2) {
      projectProbe.expectMsg(FindEntity(2))
      projectProbe.reply(EntityFound(eMeta2, e2.ref))
    }

    wf2 ! BindEntity(1)
    wf2Copy ! BindEntity(1)
    for (_ <- 1 to 2) {
      projectProbe.expectMsg(FindEntity(1))
      projectProbe.reply(EntityFound(eMeta1, e1.ref))
    }


    system.actorOf(WorkflowSetDiffer(Set(wf1, wf2), Set(wf2Copy, wf1Copy), receiver.ref))
    for (_ <- 1 to 4) {
      e1.expectMsg(GetEntityMeta())
      e1.reply(EntityMetaResponse(eMeta1))
    }
    for (_ <- 1 to 4) {
      e2.expectMsg(GetEntityMeta())
      e2.reply(EntityMetaResponse(eMeta2))
    }

    receiver.expectMsg(WorkflowSetEquals(Set(wf1, wf2), Set(wf2Copy, wf1Copy)))
  }
}