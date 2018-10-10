package ru.sber.cb.ap.gusli.actor.core.diff.category

import akka.actor.ActorRef
import akka.testkit.TestProbe
import ru.sber.cb.ap.gusli.actor.core.Category._
import ru.sber.cb.ap.gusli.actor.core.Project.{CategoryRoot, GetCategoryRoot}
import ru.sber.cb.ap.gusli.actor.core.diff.CategoryDiffer
import ru.sber.cb.ap.gusli.actor.core.diff.CategoryDiffer.CategoryDelta
import ru.sber.cb.ap.gusli.actor.core.{ActorBaseTest, CategoryMetaDefault, Project, ProjectMetaDefault}

class CategoryDiffForCategoryWithSubcategoryAndWithoutSubcategoriesSpec extends ActorBaseTest("CategoryDiffForCategoryWithSubcategoryAndWithoutSubcategoriesSpec") {
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

  "create CategoryDiffer" in {
    system.actorOf(CategoryDiffer(currentCatRoot, prevCatRoot, receiver.ref))
    receiver.expectMsgPF() {
      case CategoryDelta(rootCatDelta) =>
        rootCatDelta ! GetSubcategories()
        expectMsgPF() {
          case SubcategorySet(subs) =>
            val cat1 = subs.head
            cat1 ! GetCategoryMeta()
            expectMsg(CategoryMetaResponse(CategoryMetaDefault("cat1")))

            cat1 ! GetSubcategories()
            expectMsgPF() {
              case SubcategorySet(subs1) =>
                assert(subs1.size==1)
                println(Console.RED + subs1 + Console.RESET)
            }
        }
        rootCatDelta ! GetCategoryMeta()
        expectMsgPF() {
          case CategoryMetaResponse(m) =>
            println(m)
        }
    }
  }
}