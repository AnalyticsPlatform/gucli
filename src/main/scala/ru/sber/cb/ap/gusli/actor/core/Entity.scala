package ru.sber.cb.ap.gusli.actor.core

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor._
import ru.sber.cb.ap.gusli.actor.core.Entity._
import ru.sber.cb.ap.gusli.actor.core.Project.FindEntity
import ru.sber.cb.ap.gusli.actor.core.search.EntitySearcher

import scala.collection.immutable.HashMap

object Entity {
  def apply(meta: EntityMeta): Props = Props(new Entity(meta))

  sealed trait AbstractParentResponse extends Response

  case class GetEntityMeta(replyTo: Option[ActorRef] = None) extends Request

  case class AddChildEntity(meta: EntityMeta, replyTo: Option[ActorRef] = None) extends Request

  case class GetChildren(replyTo: Option[ActorRef] = None) extends Request

  case class GetParent(replyTo: Option[ActorRef] = None) extends Request

  //

  object NoParentResponse extends AbstractParentResponse

  case class ParentResponse(actorRef: ActorRef) extends ActorResponse with AbstractParentResponse

  case class EntityMetaResponse(meta: EntityMeta) extends Response

  case class EntityCreated(actorRef: ActorRef) extends ActorResponse

  case class ChildrenEntityList(actorList: Seq[ActorRef]) extends ActorListResponse

}

case class Entity(meta: EntityMeta) extends BaseActor {
  private var children: HashMap[Long, ActorRef] = HashMap.empty[Long, ActorRef]

  override def receive: Receive = {
    case m@GetEntityMeta(sendTo) => sendEntityMeta(sendTo)
    case msg@AddChildEntity(m, sendTo) =>
      log.debug("{}", msg)
      val replyTo = sendTo.getOrElse(sender)
      val fromRegistry = children get m.id
      if (fromRegistry.isEmpty) {
        val newEntity = context actorOf Entity(m)
        children = children + (m.id -> newEntity)
        replyTo ! EntityCreated(newEntity)
      } else replyTo ! EntityCreated(fromRegistry.get)
    case GetChildren(sendTo) =>
      sendTo.getOrElse(sender) ! ChildrenEntityList(children.values.toSeq)
    case m@FindEntity(entityId, sendTo) =>
      val searcher = context actorOf EntitySearcher(children.values.toSeq, entityId, sendTo.getOrElse(sender))
      searcher forward m
    case GetParent(sendTo) =>
      val replyTo = sendTo.getOrElse(sender)
      if (meta.id == 0)
        replyTo ! NoParentResponse
      else
        replyTo ! ParentResponse(context.parent)

  }

  private def sendEntityMeta(sendTo: Option[ActorRef]) = {
    sendTo.getOrElse(sender) ! EntityMetaResponse(meta)
  }
}

trait EntityMeta {
  def id: Long

  def name: String

  def path: String

  //todo: Find out if it needs to be implemented.
  def parentId: Option[Long]

  def storage = "HDFS"
}

case class EntityMetaDefault(id: Long, name: String, path: String, parentId: Option[Long]) extends EntityMeta