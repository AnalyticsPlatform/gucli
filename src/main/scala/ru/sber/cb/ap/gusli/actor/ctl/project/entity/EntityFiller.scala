package ru.sber.cb.ap.gusli.actor.ctl.project.entity

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}
import ru.sber.cb.ap.gusli.actor.core.Entity.{AddChildEntity, EntityCreated}
import ru.sber.cb.ap.gusli.actor.core.EntityMeta
import ru.sber.cb.ap.gusli.actor.ctl.project.entity.EntityFiller.EntityFilled

object EntityFiller {
  def apply(parentEntity: ActorRef, entityLeaf: EntityLeaf, receiver: ActorRef): Props = Props(new EntityFiller(parentEntity, entityLeaf, receiver))

  case class EntityFilled(entity: ActorRef, meta: EntityMeta) extends Response
}

class EntityFiller(parentEntity: ActorRef, entityLeaf: EntityLeaf, receiver: ActorRef) extends BaseActor {
  private var filledCount = 0
  override def preStart(): Unit = parentEntity ! AddChildEntity(entityLeaf.meta, Some(self))
  
  override def receive: Receive = {
    case EntityCreated(entity) =>
      entityLeaf.children.foreach(leaf => context.actorOf(EntityFiller(entity, leaf, self)))
      checkFinish()

    case EntityFilled(_, _) =>
      filledCount += 1
      checkFinish()
      
  }
  
  
  private def checkFinish(): Unit = if (filledCount == entityLeaf.children.size)
  finish()
  
  private def finish() = {
    receiver ! EntityFilled(parentEntity, entityLeaf.meta)
    context.stop(self)
  }
}

