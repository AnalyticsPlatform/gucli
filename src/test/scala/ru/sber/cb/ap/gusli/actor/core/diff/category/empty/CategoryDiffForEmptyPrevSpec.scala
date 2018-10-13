//package ru.sber.cb.ap.gusli.actor.core.diff.category.empty
//
//import akka.actor.ActorRef
//import akka.testkit.TestProbe
//import ru.sber.cb.ap.gusli.actor.core.Category._
//import ru.sber.cb.ap.gusli.actor.core._
//import ru.sber.cb.ap.gusli.actor.core.diff.CategoryDiffer
//import ru.sber.cb.ap.gusli.actor.core.diff.CategoryDiffer.CategoryDelta
//
//class CategoryDiffForEmptyPrevSpec extends ActorBaseTest("CategoryDiffForEmptyPrevSpec") {
//
//  private val projectProbe = TestProbe()
//  private val projectProbe2 = TestProbe()
//  private val receiverProbe = TestProbe()
//  private val category = system.actorOf(Category(CategoryMetaDefault("category"), projectProbe.ref), "category")
//  private val categoryCopy = system.actorOf(Category(CategoryMetaDefault("category"), projectProbe2.ref), "category-copy")
//  private var cat1: ActorRef = _
//  private var cat11: ActorRef = _
//  category ! AddSubcategory(CategoryMetaDefault("cat-1"))
//  expectMsgPF() {
//    case SubcategoryCreated(c) =>
//      cat1 = c
//  }
//
//  categoryCopy ! AddSubcategory(CategoryMetaDefault("cat-1"))
//  expectMsgPF() {
//    case SubcategoryCreated(c) =>
//      cat1 = c
//  }
//
//  cat1 ! AddSubcategory(CategoryMetaDefault("cat-11"))
//  expectMsgPF() {
//    case SubcategoryCreated(c) =>
//      cat11 = c
//  }
//
//  system.actorOf(CategoryDiffer(category, categoryCopy, receiverProbe.ref))
//  receiverProbe.expectMsgPF() {
//    case CategoryDelta(d) =>
//      d ! GetCategoryMeta()
//      expectMsgPF() {
//        case CategoryMetaResponse(m) =>
//          assert(m == CategoryMetaDefault("category"))
//      }
//
//      d ! GetSubcategories()
//      expectMsgPF() {
//        case SubcategorySet(s) =>
//          val c1 = s.head
//          c1 ! GetCategoryMeta()
//          expectMsgPF() {
//            case CategoryMetaResponse(m) =>
//              assert(m == CategoryMetaDefault("cat-1"))
//          }
//
//          c1 ! GetSubcategories()
//          expectMsgPF() {
//            case SubcategorySet(ss) =>
//              val c11 = ss.head
//              c11 ! GetCategoryMeta()
//              expectMsgPF() {
//                case CategoryMetaResponse(m) =>
//                  assert(m == CategoryMetaDefault("cat-11"))
//              }
//
//              c11 ! GetSubcategories()
//              expectMsgPF() {
//                case SubcategorySet(sss) =>
//                  assert(sss.isEmpty)
//              }
//          }
//      }
//  }
//}
//
//
