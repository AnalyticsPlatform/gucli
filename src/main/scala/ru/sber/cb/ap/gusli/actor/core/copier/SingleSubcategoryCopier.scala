package ru.sber.cb.ap.gusli.actor.core.copier

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}


object SingleSubcategoryCopier {
  def apply(toProject: ActorRef, fromProject: ActorRef, toCategory: ActorRef, fromCategory: ActorRef, receiver: ActorRef): Props =
    Props(new SingleSubcategoryCopier(toProject, fromProject, toCategory, fromCategory, receiver))

  case class SubcategoryCloneSuccessful(clonedCategory: ActorRef, fromCategory: ActorRef) extends Response

}

class SingleSubcategoryCopier(toProject: ActorRef, fromProject: ActorRef, toCategory: ActorRef, fromCategory: ActorRef, receiver: ActorRef) extends BaseActor {

  import SingleSubcategoryCopier._
  import ru.sber.cb.ap.gusli.actor.core.Category._

  override def preStart(): Unit = {
    fromCategory ! GetCategoryMeta()
  }

  override def receive: Receive = {
    case CategoryMetaResponse(m) =>
      toCategory ! AddSubcategory(m)
    case SubcategoryCreated(cloned) =>
      receiver ! SubcategoryCloneSuccessful(cloned, fromCategory)
      context.stop(self)
  }
}
