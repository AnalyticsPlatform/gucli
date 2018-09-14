package ru.sber.cb.ap.gusli.actor.core

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Request, Response}

object Project {
  def apply(meta: ProjectMeta): Props = {
    Props(new Project(meta))
  }
  
  case class GetProjectMeta(replyTo: Option[ActorRef] = None) extends Request

  case class GetCategoryRoot(replyTo: Option[ActorRef] = None) extends Request

  //entity requests
  case class GetEntityRoot(replyTo: Option[ActorRef] = None) extends Request
  case class FindEntity(entityId: Long, replyTo: Option[ActorRef] = None) extends Request

  //entity responses
  case class EntityFound(meta: EntityMeta, entityRef: ActorRef) extends Response
  case class EntityNotFound(entityId: Long) extends Response


  case class ProjectMetaResponse(name: String) extends Response with ProjectMeta

  case class CategoryRoot(root: ActorRef) extends Response

  case class EntityRoot(root: ActorRef) extends Response
}

class Project(meta: ProjectMeta) extends BaseActor {
  import Project._
  override def receive: Receive = {
    case GetProjectMeta(sendTo) => sendTo getOrElse sender ! ProjectMetaResponse(meta.name)
    case GetCategoryRoot(sendTo) => sendTo getOrElse sender ! CategoryRoot(context.self)
    case GetEntityRoot(sendTo) => sendTo getOrElse sender ! EntityRoot(context.self)
  }
}

trait ProjectMeta {
  def name: String
}

case class ProjectMetaDefault(name:String) extends ProjectMeta