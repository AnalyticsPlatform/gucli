package ru.sber.cb.ap.gusli.actor.core.copier

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}
import akka.pattern.ask
import akka.util.Timeout
import concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

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
      implicit val timeout = Timeout(5 hour)
      (toCategory ? GetCategoryMeta()).map(
        x=>{
          log.debug("{}",x)
        }
      )
      toCategory ! AddSubcategory(m)
    case SubcategoryCreated(cloned) =>
      receiver ! SubcategoryCloneSuccessful(cloned, fromCategory)
      context.stop(self)
  }
}
