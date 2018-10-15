package ru.sber.cb.ap.gusli.actor.core.extractor

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.dto.CategoryDto
import ru.sber.cb.ap.gusli.actor.core.extractor.EntityIdFromCategoryDtoExtractor.EntityIdFromCategoryDtoExtracted
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}

object EntityIdFromCategoryDtoExtractor {
  def apply(categoryDto: CategoryDto, receiver: ActorRef): Props = Props(new EntityIdFromCategoryDtoExtractor(categoryDto, receiver))

  case class EntityIdFromCategoryDtoExtracted(ids: Set[Long]) extends Response

}

class EntityIdFromCategoryDtoExtractor(categoryDto: CategoryDto, receiver: ActorRef) extends BaseActor {
  var subcategoriesCount = 0
  var ids: Set[Long] = Set.empty

  override def preStart(): Unit = {
    val entityIds: Set[Long] =
      (for (wf <- categoryDto.workflows; e <- wf.entities)
        yield e) ++
        (for (e <- categoryDto.entities)
          yield e)

    for (sub <- categoryDto.subcategories) {
      context.actorOf(EntityIdFromCategoryDtoExtractor(sub, self))
      subcategoriesCount += 1
    }

    if (subcategoriesCount == 0) {
      receiver ! EntityIdFromCategoryDtoExtracted(entityIds)
      context.stop(self)
    }
    else
      ids ++= entityIds
  }

  override def receive: Receive = {
    case EntityIdFromCategoryDtoExtracted(e) =>
      ids ++= e
      subcategoriesCount -= 1
      if (subcategoriesCount == 0) {
        receiver ! EntityIdFromCategoryDtoExtracted(ids)
        context.stop(self)
      }
  }
}