package ru.sber.cb.ap.gusli.actor.core.clone

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.Entity._
import ru.sber.cb.ap.gusli.actor.core.EntityMeta
import ru.sber.cb.ap.gusli.actor.core.clone.EntityHierarchyCloner.{EntityHierarchyCloneSuccessful, RootEntityCantBeCloned}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}

object EntityHierarchyCloner {
  def apply(entity: ActorRef, toRoot: ActorRef, receiver: ActorRef): Props = Props(new EntityHierarchyCloner(entity, toRoot, receiver))

  sealed abstract class AbstractEntityHierarchyClone extends Response

  case class EntityHierarchyCloneSuccessful(entity: ActorRef, meta: EntityMeta) extends AbstractEntityHierarchyClone

  case object RootEntityCantBeCloned extends AbstractEntityHierarchyClone

}


class EntityHierarchyCloner(entity: ActorRef, toRoot: ActorRef, receiver: ActorRef) extends BaseActor {
  var meta: Option[EntityMeta] = None
  var entityToAddChild: Option[ActorRef] = None

  override def preStart(): Unit = {
    entity ! GetParent()
    entity ! GetEntityMeta()
  }


  override def receive: Receive = {
    case EntityMetaResponse(m) =>
      meta = Some(m)
      createEntity()
    case NoParentResponse =>
      receiver ! RootEntityCantBeCloned
      context.stop(self)
    case ParentResponse(p) =>
      context.actorOf(EntityHierarchyCloner(p, toRoot, self))
    case RootEntityCantBeCloned =>
      entityToAddChild = Some(toRoot)
      createEntity()
    case EntityHierarchyCloneSuccessful(e, _) =>
      entityToAddChild = Some(e)
      createEntity()
    case EntityCreated(e) =>
      receiver ! EntityHierarchyCloneSuccessful(e, meta.get)
      context.stop(self)
  }

  def createEntity(): Unit = {
    for (m <- meta; e <- entityToAddChild) {
      e ! AddChildEntity(m)
    }
  }
}
