package ru.sber.cb.ap.gusli.actor.core.clone

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.clone.CategorySetCloner.CategorySetCloneSuccessfully
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}

object CategorySetCloner {
  def apply(targetCategory: ActorRef, sourceSubCats: Set[ActorRef], receiver: ActorRef): Props =
    Props(new CategorySetCloner(targetCategory, sourceSubCats, receiver))

  case class CategorySetCloneSuccessfully() extends Response

}


class CategorySetCloner(targetCategory: ActorRef, sourceSubCats: Set[ActorRef], receiver: ActorRef) extends BaseActor {

  import ru.sber.cb.ap.gusli.actor.core.clone.CategoryCloner._

  var sourceCounts = 0

  override def preStart(): Unit = {
    sourceCounts = sourceSubCats.size
    for (sub <- sourceSubCats)
      context.actorOf(CategoryCloner(targetCategory, sub, self))
  }

  override def receive: Receive = {
    case CategoryCloneSuccessfully() =>
      sourceCounts -= 1
      if (sourceCounts == 0) {
        receiver ! CategorySetCloneSuccessfully()
      }
  }

}
