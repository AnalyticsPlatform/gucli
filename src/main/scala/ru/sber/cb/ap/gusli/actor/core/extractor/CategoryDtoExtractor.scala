package ru.sber.cb.ap.gusli.actor.core.extractor

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.Category._
import ru.sber.cb.ap.gusli.actor.core.CategoryMeta
import ru.sber.cb.ap.gusli.actor.core.dto.{CategoryDto, WorkflowDto}
import ru.sber.cb.ap.gusli.actor.core.extractor.CategoryDtoExtractor.CategoryDtoExtracted
import ru.sber.cb.ap.gusli.actor.core.extractor.WorkflowDtoExtractor.WorkflowExtracted
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}

object CategoryDtoExtractor {
  def apply(category: ActorRef, receiver: ActorRef): Props = Props(new CategoryDtoExtractor(category, receiver))

  case class CategoryDtoExtracted(dto: CategoryDto) extends Response

}

class CategoryDtoExtractor(category: ActorRef, receiver: ActorRef) extends BaseActor {
  private var categoryMeta: Option[CategoryMeta] = None
  private var subs: Set[CategoryDto] = Set.empty
  private var wfs: Set[WorkflowDto] = Set.empty
  private var subcategoryCount: Int = -1
  private var workflowCount: Int = -1

  override def preStart(): Unit = {
    category ! GetCategoryMeta()
    category ! GetSubcategories()
    category ! GetWorkflows()
  }


  def receive(): Receive = {
    case CategoryMetaResponse(m) =>
      categoryMeta = Some(m)
      checkFinish()
    case SubcategorySet(s) =>
      subcategoryCount = s.size
      for (c <- s)
        context.actorOf(CategoryDtoExtractor(c, self))
      checkFinish()
    case WorkflowSet(s) =>
      workflowCount = s.size
      for (w <- s)
        context.actorOf(WorkflowDtoExtractor(w, self))
      checkFinish()
    case CategoryDtoExtracted(dto) =>
      subs += dto
      subcategoryCount -= 1
      checkFinish()
    case WorkflowExtracted(dto) =>
      wfs += dto
      workflowCount -= 1
      checkFinish()
  }


  def checkFinish(): Unit =
    if (isSubcategoriesAndWorkflowsExtracted())
      for (m <- categoryMeta) {
        val dto = CategoryDto(m, subs, wfs)
        receiver ! CategoryDtoExtracted(dto)
        context.stop(self)
      }


  def isSubcategoriesAndWorkflowsExtracted(): Boolean =
    subcategoryCount == 0 && workflowCount == 0
}