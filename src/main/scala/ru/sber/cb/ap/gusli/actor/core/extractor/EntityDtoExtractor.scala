package ru.sber.cb.ap.gusli.actor.core.extractor

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.Entity.{ChildrenEntityList, EntityMetaResponse, GetChildren, GetEntityMeta}
import ru.sber.cb.ap.gusli.actor.core.EntityMeta
import ru.sber.cb.ap.gusli.actor.core.dto.EntityDto
import ru.sber.cb.ap.gusli.actor.core.extractor.EntityDtoExtractor.EntityDtoExtracted
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}

object EntityDtoExtractor {
  def apply(entity: ActorRef, receiver: ActorRef): Props = Props(new EntityDtoExtractor(entity, receiver))

  case class EntityDtoExtracted(dto: EntityDto) extends Response

}

class EntityDtoExtractor(entity: ActorRef, receiver: ActorRef) extends BaseActor {
  var meta: Option[EntityMeta] = None
  var childrenCount = -1
  var children: Set[EntityDto] = Set.empty

  override def preStart(): Unit = {
    entity ! GetEntityMeta()
    entity ! GetChildren()
  }

  override def receive: Receive = {
    case EntityMetaResponse(m) =>
      meta = Some(m)
    case ChildrenEntityList(l) =>
      childrenCount = l.size
      for (c <- l)
        context.actorOf(EntityDtoExtractor(c, self))

      checkFinish()
    case EntityDtoExtracted(d) =>
      children += d
      childrenCount -= 1

      checkFinish()
  }

  def checkFinish(): Unit = {
    for (m <- meta) {
      if (childrenCount == 0) {
        receiver ! EntityDtoExtracted(EntityDto(m, children))
      }
    }
  }
}