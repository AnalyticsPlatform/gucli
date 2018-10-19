package ru.sber.cb.ap.gusli.actor.core.diff.project.recursive

import akka.actor.ActorRef
import akka.testkit.TestProbe
import ru.sber.cb.ap.gusli.actor.core.Category._
import ru.sber.cb.ap.gusli.actor.core.Project.{CategoryRoot, GetCategoryRoot}
import ru.sber.cb.ap.gusli.actor.core.diff.ProjectDiffer
import ru.sber.cb.ap.gusli.actor.core.diff.ProjectDiffer.ProjectDelta
import ru.sber.cb.ap.gusli.actor.core.{ActorBaseTest, CategoryMetaDefault, Project, ProjectMetaDefault, WorkflowMetaDefault}

class ProjectDiffForNonEqualsOf3DepthSpec extends ActorBaseTest("ProjectDiffForNonEqualsOf3DepthSpec") {
  private val receiver = TestProbe()
  private val currentProject: ActorRef = system.actorOf(Project(ProjectMetaDefault("project")))
  private val prevProject: ActorRef = system.actorOf(Project(ProjectMetaDefault("project-copy")))
  private var currentCatRoot: ActorRef = _
  private var prevCatRoot: ActorRef = _
  private var currCat1: ActorRef = _
  private var currCat11: ActorRef = _
  private var prevCat1: ActorRef = _

  currentProject ! GetCategoryRoot()
  expectMsgPF() {
    case CategoryRoot(r) =>
      currentCatRoot = r
  }
  prevProject ! GetCategoryRoot()
  expectMsgPF() {
    case CategoryRoot(r) =>
      prevCatRoot = r
  }

  currentCatRoot ! AddSubcategory(CategoryMetaDefault("cat1"))
  expectMsgPF() {
    case SubcategoryCreated(s) =>
      currCat1 = s
  }

  prevCatRoot ! AddSubcategory(CategoryMetaDefault("cat1"))
  expectMsgPF() {
    case SubcategoryCreated(s) =>
      prevCat1 = s
  }

  currCat1 ! AddSubcategory(CategoryMetaDefault("cat11"))
  expectMsgPF() {
    case SubcategoryCreated(s) =>
      currCat11 = s
  }

  currCat11 ! CreateWorkflow(WorkflowMetaDefault("wf-11-1", Map("sql" -> "select 1 as a")))

  "create ProjectDiffer" in {
    system.actorOf(ProjectDiffer(currentProject, prevProject, receiver.ref))
    receiver.expectMsgPF() {
      case ProjectDelta(p) =>
        assert(p.name == "project")
        val cat1 = p.categoryRoot.subcategories.head
        assert(cat1.name == "cat1")

        val cat11 = cat1.subcategories.head
        assert(cat11.name == "cat11")

        val wf111 = cat11.workflows.head
        assert(wf111.name == "wf-11-1")
        assert(wf111.sql == Map("sql" -> "select 1 as a"))
    }
  }
}