package ru.sber.cb.ap.gusli.actor.core.diff

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.CategoryMeta
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}


object CategoryMetaDiffer {
  def apply(currentCat: ActorRef, prevCat: ActorRef, receiver: ActorRef): Props = Props(new CategoryMetaDiffer(currentCat, prevCat, receiver))


  abstract class AbstractCategoryMetaResponse extends Response

  case class CategoryMetaEquals(currentCat: ActorRef, prevCat: ActorRef, meta: CategoryMeta) extends AbstractCategoryMetaResponse

  case class CategoryMetaDelta(deltaCat: CategoryMeta) extends AbstractCategoryMetaResponse

}


class CategoryMetaDiffer(currentCat: ActorRef, prevCat: ActorRef, receiver: ActorRef) extends BaseActor {

  import ru.sber.cb.ap.gusli.actor.core.Category._
  import ru.sber.cb.ap.gusli.actor.core.diff.CategoryMetaDiffer._

  private var currentMeta: Option[CategoryMeta] = None
  private var prevMeta: Option[CategoryMeta] = None

  override def preStart(): Unit = {
    currentCat ! GetCategoryMeta()
    prevCat ! GetCategoryMeta()
  }


  override def receive: Receive = {
    case CategoryMetaResponse(m) =>
      if (sender() == currentCat) currentMeta = Some(m)
      else if (sender() == prevCat) prevMeta = Some(m)
      else throw new RuntimeException(s"Unknown sender:${sender()}")

      for (c <- currentMeta; p <- prevMeta) {
        if (c == p)
          receiver ! CategoryMetaEquals(currentCat, prevCat, c)
        else
          receiver ! CategoryMetaDelta(c)

        context.stop(self)
      }
  }
}
