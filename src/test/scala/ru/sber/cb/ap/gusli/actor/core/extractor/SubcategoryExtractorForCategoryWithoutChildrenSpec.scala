package ru.sber.cb.ap.gusli.actor.core.extractor

import akka.testkit.TestProbe
import ru.sber.cb.ap.gusli.actor.core._
import ru.sber.cb.ap.gusli.actor.core.extractor.SubcategoryExtractor.SubcategoryExtracted

class SubcategoryExtractorForCategoryWithoutChildrenSpec extends ActorBaseTest("SubcategoryExtractorForCategoryWithoutChildrenSpec") {
  private val projectProbe: TestProbe = TestProbe()
  private val cat = system.actorOf(Category(CategoryMetaDefault("category", Map.empty), projectProbe.ref), "category")
  private val receiverProbe = TestProbe()

  "An SubcategoryExtractor" when {
    "category without children" should {
      system.actorOf(SubcategoryExtractor(cat, receiverProbe.ref))
      "return empty map" in {
        receiverProbe.expectMsg(SubcategoryExtracted(cat, Map.empty))
      }
    }
  }
}