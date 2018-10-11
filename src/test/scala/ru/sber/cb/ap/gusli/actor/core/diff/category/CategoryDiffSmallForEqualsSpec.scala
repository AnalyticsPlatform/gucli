package ru.sber.cb.ap.gusli.actor.core.diff.category

import akka.actor.ActorRef
import akka.testkit.TestProbe
import ru.sber.cb.ap.gusli.actor.core.Category._
import ru.sber.cb.ap.gusli.actor.core.Project.{CategoryRoot, GetCategoryRoot}
import ru.sber.cb.ap.gusli.actor.core.diff.CategoryDiffer
import ru.sber.cb.ap.gusli.actor.core.diff.CategoryDiffer.CategoryEquals
import ru.sber.cb.ap.gusli.actor.core.{ActorBaseTest, CategoryMetaDefault, Project, ProjectMetaDefault}

class CategoryDiffSmallForEqualsSpec extends ActorBaseTest("CategoryDiffSmallForEqualsSpec") {
  private val receiver = TestProbe()
  private val currentProject: ActorRef = system.actorOf(Project(ProjectMetaDefault("project")))
  private val prevProject: ActorRef = system.actorOf(Project(ProjectMetaDefault("project-copy")))
  private var currentCatRoot: ActorRef = _
  private var prevCatRoot: ActorRef = _
  private var currCat1: ActorRef = _
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

  "create CategoryDiffer" in {
    system.actorOf(CategoryDiffer(currentCatRoot, prevCatRoot, receiver.ref))
    receiver.expectMsgClass(classOf[CategoryEquals])
  }
}