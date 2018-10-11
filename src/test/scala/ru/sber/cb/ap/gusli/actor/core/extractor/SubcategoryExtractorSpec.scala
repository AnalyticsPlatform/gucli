package ru.sber.cb.ap.gusli.actor.core.extractor

import akka.actor.ActorRef
import akka.testkit.TestProbe
import ru.sber.cb.ap.gusli.actor.core.Category.{AddSubcategory, SubcategoryCreated}
import ru.sber.cb.ap.gusli.actor.core._
import ru.sber.cb.ap.gusli.actor.core.extractor.SubcategoryExtractor.SubcategoryExtracted

class SubcategoryExtractorSpec extends ActorBaseTest("SubcategoryExtractorSpec") {
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
  cat ! AddSubcategory(CategoryMetaDefault("cat-2", Map.empty))
  expectMsgPF() {
    case SubcategoryCreated(c) =>
      cat2 = c
  }

  "An SubcategoryExtractor" when {
    "created" should {
      system.actorOf(SubcategoryExtractor(cat, receiverProbe.ref))
      "return Map with subcategories and it's names" in {
        receiverProbe.expectMsg(SubcategoryExtracted(cat, Map("cat-1" -> cat1, "cat-2" -> cat2)))
      }
    }


  }
}