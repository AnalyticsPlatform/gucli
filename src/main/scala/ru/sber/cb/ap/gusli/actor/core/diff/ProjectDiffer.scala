package ru.sber.cb.ap.gusli.actor.core.diff

import akka.actor.Actor.emptyBehavior
import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.{Entity, EntityMetaDefault, ProjectMeta}
import ru.sber.cb.ap.gusli.actor.core.Project._
import ru.sber.cb.ap.gusli.actor.core.clone.EntityCloner
import ru.sber.cb.ap.gusli.actor.core.clone.EntityCloner.EntitiesCloneSuccessful
import ru.sber.cb.ap.gusli.actor.core.diff.CategoryDtoDiffer.{CategoryDtoDelta, CategoryDtoEquals}
import ru.sber.cb.ap.gusli.actor.core.diff.ProjectDiffer.{ProjectDelta, ProjectEquals}
import ru.sber.cb.ap.gusli.actor.core.dto.{CategoryDto, EntityDto, ProjectDto}
import ru.sber.cb.ap.gusli.actor.core.extractor.{CategoryDtoExtractor, EntityDtoExtractor, EntityIdFromCategoryDtoExtractor}
import ru.sber.cb.ap.gusli.actor.core.extractor.CategoryDtoExtractor.CategoryDtoExtracted
import ru.sber.cb.ap.gusli.actor.core.extractor.EntityDtoExtractor.EntityDtoExtracted
import ru.sber.cb.ap.gusli.actor.core.extractor.EntityIdFromCategoryDtoExtractor.EntityIdFromCategoryDtoExtracted
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}

object ProjectDiffer {
  def apply(currentProject: ActorRef, prevProject: ActorRef, receiver: ActorRef): Props = Props(new ProjectDiffer(currentProject, prevProject, receiver))

  abstract class ProjectDifferResponse extends Response

  case class ProjectEquals(currentProject: ActorRef, prevProject: ActorRef) extends ProjectDifferResponse

  case class ProjectDelta(deltaProject: ProjectDto) extends ProjectDifferResponse

}


class ProjectDiffer(currentProject: ActorRef, prevProject: ActorRef, receiver: ActorRef) extends BaseActor {
  private var currProjMeta: Option[ProjectMeta] = None
  private var currEntityDto: Option[EntityDto] = None
  private var currRootEntity: Option[ActorRef] = None
  private var categoryDeltaDto: Option[CategoryDto] = None

  private var entityIds: Option[Set[Long]] = None
  private var stubRootEntity = context.actorOf(Entity(EntityMetaDefault(0, "entity", "", None)), "entity")

  private var currCatExtractor: ActorRef = _
  private var prevCatExtractor: ActorRef = _
  private var currCatDto: Option[CategoryDto] = None
  private var prevCatDto: Option[CategoryDto] = None


  override def preStart(): Unit = {
    currentProject ! GetProjectMeta()
    currentProject ! GetEntityRoot()
    currentProject ! GetCategoryRoot()
    prevProject ! GetCategoryRoot()
  }

  override def receive: Receive = {
    case CategoryRoot(r) =>
      if (sender() == currentProject)
        currCatExtractor = context.actorOf(CategoryDtoExtractor(r, self))
      else if (sender() == prevProject)
        prevCatExtractor = context.actorOf(CategoryDtoExtractor(r, self))
    case CategoryDtoExtracted(dto) =>
      if (sender() == currCatExtractor)
        currCatDto = Some(dto)
      else if (sender() == prevCatExtractor)
        prevCatDto = Some(dto)

      getCategoryDiff()

    case ProjectMetaResponse(m) =>
      currProjMeta = Some(m)
      exportProject()
    case EntityRoot(r) =>
      currRootEntity = Some(r)
      cloneEntityToStub()
    case EntityIdFromCategoryDtoExtracted(ids) =>
      entityIds = Some(ids)
      cloneEntityToStub()
    case EntitiesCloneSuccessful =>
      context.actorOf(EntityDtoExtractor(stubRootEntity, self))
    case EntityDtoExtracted(entityDto) =>
      currEntityDto=Some(entityDto)
      exportProject()
  }

  def getCategoryDiff(): Unit = {
    for (c <- currCatDto; p <- prevCatDto) {
      CategoryDtoDiffer.getDiff(c, p) match {
        case CategoryDtoEquals(_, _) =>
          receiver ! ProjectEquals(currentProject, prevProject)
          context.stop(self)
        case CategoryDtoDelta(d) =>
          context.actorOf(EntityIdFromCategoryDtoExtractor(d, self))
          categoryDeltaDto = Some(d)
      }
    }
  }

  def cloneEntityToStub(): Unit = {
    for (currRoot <- currRootEntity; ids <- entityIds) {
      context.actorOf(EntityCloner(currRoot, stubRootEntity, ids, self))
    }
  }

  def exportProject(): Unit ={
    for (m<-currProjMeta; e<-currEntityDto; c<-categoryDeltaDto) {
      val project = ProjectDto(m.name, e, c)
      receiver ! ProjectDelta(project)
      context.stop(self)
    }
  }
}
