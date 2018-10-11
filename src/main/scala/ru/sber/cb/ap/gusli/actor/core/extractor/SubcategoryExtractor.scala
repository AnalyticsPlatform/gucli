package ru.sber.cb.ap.gusli.actor.core.extractor

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.Category.{CategoryMetaResponse, GetCategoryMeta, GetSubcategories, SubcategorySet}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}
import ru.sber.cb.ap.gusli.actor.core.Entity.{EntityMetaResponse, GetEntityMeta}
import ru.sber.cb.ap.gusli.actor.core.Workflow.{EntityList, GetEntityList}
import ru.sber.cb.ap.gusli.actor.core.extractor.EntityIdExtractor.EntityIdExtracted
import ru.sber.cb.ap.gusli.actor.core.extractor.SubcategoryExtractor.SubcategoryExtracted

import scala.collection.immutable.HashMap


object SubcategoryExtractor {
  def apply(category: ActorRef, receiver: ActorRef): Props = Props(new SubcategoryExtractor(category, receiver))

  case class SubcategoryExtracted(category: ActorRef, subs: Map[String, ActorRef]) extends Response

}

class SubcategoryExtractor(category: ActorRef, receiver: ActorRef) extends BaseActor {
  private var subCount = 0
  private var subs: HashMap[String, ActorRef] = HashMap.empty

  override def preStart(): Unit = {
    category ! GetSubcategories()
  }

  override def receive: Receive = {
    case SubcategorySet(set) =>
      if (set.isEmpty) {
        receiver ! SubcategoryExtracted(category, Map.empty)
      }
      else if (set.nonEmpty){
        subCount = set.size
        for (c <- set)
          c ! GetCategoryMeta()
      }

    case CategoryMetaResponse(m) =>
      subs += m.name -> sender()
      subCount -= 1
      if (subCount == 0) {
        receiver ! SubcategoryExtracted(category, subs)
        context.stop(self)
      }

  }
}