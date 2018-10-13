//package ru.sber.cb.ap.gusli.actor.core.diff.category
//
//import akka.testkit.TestProbe
//import ru.sber.cb.ap.gusli.actor.core._
//import ru.sber.cb.ap.gusli.actor.core.diff.CategoryDiffer
//
//class CategoryDiffForEqualsSpec extends ActorBaseTest("CategoryDiffForEqualsSpec") {
//
//  import ru.sber.cb.ap.gusli.actor.core.diff.CategoryDiffer._
//
//  private val projectProbe = TestProbe()
//  private val receiverProbe = TestProbe()
//  private val meta1 = CategoryMetaDefault("category", Map("p1" -> "111", "p2" -> "222"))
//  private val meta2 = CategoryMetaDefault("category", Map("p2" -> "222", "p1" -> "111"))
//  private val currentCat = system.actorOf(Category(meta1, projectProbe.ref))
//  private val prevCat = system.actorOf(Category(meta2, projectProbe.ref))
//
//  "CategoryDiff for Category with the same meta" must {
//    "return CategoryEquals" in {
//      system.actorOf(CategoryDiffer(currentCat, prevCat, receiverProbe.ref))
//      receiverProbe.expectMsg(CategoryEquals(currentCat, prevCat))
//    }
//  }
//
//}
//
//
