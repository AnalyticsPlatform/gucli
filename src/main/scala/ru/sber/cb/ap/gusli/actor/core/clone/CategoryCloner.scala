package ru.sber.cb.ap.gusli.actor.core.clone

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}

object CategoryCloner {
  def apply(targetCategory: ActorRef, sourceSubCategory: ActorRef, receiver: ActorRef): Props =
    Props(new CategoryCloner(targetCategory, sourceSubCategory, receiver))

  case class CategoryCloneSuccessfully() extends Response

}

class CategoryCloner(targetCategory: ActorRef, sourceSubCategory: ActorRef, receiver: ActorRef) extends BaseActor {

  import ru.sber.cb.ap.gusli.actor.core.Category._
  import ru.sber.cb.ap.gusli.actor.core.clone.CategoryCloner._

  var targetSub: Option[ActorRef] = None
  var subs: Option[Set[ActorRef]] = None
  var subCatCount: Int = _

  override def preStart(): Unit = {
    sourceSubCategory ! GetCategoryMeta()
    sourceSubCategory ! GetSubcategories()
  }

  override def receive: Receive = {
    case CategoryMetaResponse(m) =>
      targetCategory ! AddSubcategory(m)
    case SubcategoryCreated(s) =>
      targetSub = Some(s)
      copyRecursive()
    case SubcategorySet(set) =>
      subCatCount = set.size
      subs = Some(set)
      copyRecursive()
    case CategoryCloneSuccessfully() =>
      subCatCount -= 1
      if (subCatCount == 0) {
        receiver ! CategoryCloneSuccessfully()
        context.stop(self)
      }
  }

  def copyRecursive(): Unit = {
    for (ts <- targetSub; subCats <- subs) {
      if (subCats.isEmpty) {
        receiver ! CategoryCloneSuccessfully()
        context.stop(self)
      }
      else {
        for (sub <- subCats)
          context.actorOf(CategoryCloner(ts, sub, self))
      }
    }
  }
}
