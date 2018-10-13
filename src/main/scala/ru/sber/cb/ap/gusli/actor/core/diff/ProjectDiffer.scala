package ru.sber.cb.ap.gusli.actor.core.diff

import akka.actor.Actor.emptyBehavior
import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}

object ProjectDiffer {
  def apply(currentProject: ActorRef, prevProject: ActorRef, receiver: ActorRef): Props = Props(new ProjectDiffer(currentProject, prevProject, receiver))

  abstract class ProjectDifferResponse extends Response

  case class ProjectEquals(currentProject: ActorRef, prevProject: ActorRef) extends ProjectDifferResponse

  case class ProjectDelta(deltaProject: ActorRef) extends ProjectDifferResponse

}


class ProjectDiffer(currentProject: ActorRef, prevProject: ActorRef, receiver: ActorRef) extends BaseActor {
  override def receive: Receive = emptyBehavior
}
