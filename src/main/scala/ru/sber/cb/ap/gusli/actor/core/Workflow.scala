package ru.sber.cb.ap.gusli.actor.core

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.Project.{EntityFound, EntityNotFound, FindEntity}
import ru.sber.cb.ap.gusli.actor.{ActorListResponse, BaseActor, Request, Response}

import scala.collection.immutable
import scala.collection.immutable.HashMap

object Workflow {
  def apply(meta: WorkflowMeta, project: ActorRef): Props = Props(new Workflow(meta, project))

  case class GetWorkflowMeta(replyTo: Option[ActorRef] = None) extends Request

  case class BindEntity(entityId: Long, replyTo: Option[ActorRef] = None) extends Request

  case class GetEntityList(replyTo: Option[ActorRef] = None) extends Request

  //responses
  case class WorkflowMetaResponse(workflowMeta: WorkflowMeta) extends Response

  case class BindEntitySuccessful(entityId: Long) extends Response
  case class BindEntityFailedBecauseItNotExists(entityId: Long) extends Response

  //todo: replace to ActorSetResponse
  case class EntityList(actorList: Seq[ActorRef]) extends ActorListResponse
}

class Workflow(meta: WorkflowMeta, project: ActorRef) extends BaseActor {

  import Workflow._

  var awaitEntityBind: Map[Long, immutable.List[ActorRef]] = HashMap.empty[Long, List[ActorRef]]
  var boundEntitySet: Map[Long, ActorRef] = Map.empty[Long, ActorRef]

  override def receive: Receive = {
    case GetWorkflowMeta(sendTo) => sendTo.getOrElse(sender) ! WorkflowMetaResponse(meta)

    case BindEntity(entityId, sendTo) =>
      val replyTo = sendTo.getOrElse(sender)
      val refs = awaitEntityBind.getOrElse(entityId, List[ActorRef]()).::(replyTo)

      awaitEntityBind = awaitEntityBind + (entityId -> refs)
      project ! FindEntity(entityId)

    case EntityNotFound(entityId) =>
      val refs = awaitEntityBind.getOrElse(entityId, List[ActorRef]())
      for (a <- refs)
        a ! BindEntityFailedBecauseItNotExists(entityId)

      awaitEntityBind = awaitEntityBind.filterKeys(id => id != entityId)

    case EntityFound(meta, entityRef) =>
      boundEntitySet = boundEntitySet + (meta.id -> entityRef)
      awaitEntityBind getOrElse(meta.id, Nil) foreach (_ ! BindEntitySuccessful(meta.id))
      awaitEntityBind = awaitEntityBind - meta.id

    case GetEntityList(sendTo) =>
      sendTo.getOrElse(sender)  ! EntityList(boundEntitySet.values.toSeq)
  }
}

trait WorkflowMeta {
  def name: String

  // Content
  def sql: Map[String, String]

  // Content
  def sqlMap: Map[String, String]

  // Content
  def init: Map[String, String]

  def user: Option[String]

  def queue: Option[String]

  def grenkiVersion: Option[String]

  def params: Map[String, String]

  def stats: Set[Long]
}

case class WorkflowMetaDefault(name: String,
                               sql: Map[String, String],
                               sqlMap: Map[String, String] = Map.empty,
                               init: Map[String, String] = Map.empty,
                               user: Option[String] = None,
                               queue: Option[String] = None,
                               grenkiVersion: Option[String] = None,
                               params: Map[String, String] = Map.empty,
                               stats: Set[Long] = Set.empty) extends WorkflowMeta


