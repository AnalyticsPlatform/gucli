package ru.sber.cb.ap.gusli.actor.core.diff

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.Category._
import ru.sber.cb.ap.gusli.actor.core.diff.CategoryDiffer.{CategoryDelta, CategoryEquals}
import ru.sber.cb.ap.gusli.actor.core.diff.CategoryMetaDiffer.{AbstractCategoryMetaResponse, CategoryMetaDelta, CategoryMetaEquals}
import ru.sber.cb.ap.gusli.actor.core.diff.SubCategoryMetaDiffer.{SubCategoryMetaDelta, SubCategoryMetaEquals}
import ru.sber.cb.ap.gusli.actor.core.diff.WorkflowFromCategoryDiffer.{WorkflowFromCategoryDelta, WorkflowFromCategoryEquals, WorkflowFromCategoryResponse}
import ru.sber.cb.ap.gusli.actor.core.{Category, CategoryMeta}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}


object CategoryDiffer {
  def apply(currentCat: ActorRef, prevCat: ActorRef, receiver: ActorRef): Props = Props(new CategoryDiffer(currentCat, prevCat, receiver))

  abstract class CategoryDifferResponse extends Response

  case class CategoryEquals(currentCat: ActorRef, prevCat: ActorRef) extends CategoryDifferResponse

  case class CategoryDelta(deltaCat: ActorRef) extends CategoryDifferResponse

}

class CategoryDiffer(currentCat: ActorRef, prevCat: ActorRef, receiver: ActorRef) extends BaseActor {
  var subMetaDelta: Option[Set[CategoryMeta]] = None
  var subMetaCount = 0
  private var currProject: Option[ActorRef] = None
  private var currentMeta: Option[CategoryMeta] = None
  private var categoryMetaResponse: Option[AbstractCategoryMetaResponse] = None
  private var workflowFromCategory: Option[WorkflowFromCategoryResponse] = None
  private var deltaCat: Option[ActorRef] = None
  private var isSubcategoryCompared = false

  override def preStart(): Unit = {
    currentCat ! GetProject()
    currentCat ! GetCategoryMeta()
    context.actorOf(CategoryMetaDiffer(currentCat, prevCat, self))
    context.actorOf(WorkflowFromCategoryDiffer(currentCat, prevCat, self))
    context.actorOf(SubCategoryMetaDiffer(currentCat, prevCat, self))
  }

  override def receive: Receive = {
    case ProjectResponse(p) =>
      currProject = Some(p)
      checkFinish()

    case CategoryMetaResponse(m) =>
      val cat = sender()
      if (cat == currentCat)
        currentMeta = Some(m)
      else
        throw new RuntimeException(s"Unknown sender:${sender()}")

      checkFinish()

    case r: AbstractCategoryMetaResponse =>
      categoryMetaResponse = Some(r)
      checkFinish()

    case r: WorkflowFromCategoryResponse =>
      workflowFromCategory = Some(r)
      checkFinish()

    case SubCategoryMetaEquals(_, _) =>
      isSubcategoryCompared = true
      subMetaDelta = Some(Set.empty)
      checkFinish()
    case SubCategoryMetaDelta(delta) =>
      subMetaDelta = Some(delta)
      subMetaCount = delta.size
      checkFinish()
    case SubcategoryCreated(_) =>
      subMetaCount -= 1
      isSubcategoryCompared = subMetaCount == 0
      checkFinish()
  }


  def checkFinish(): Unit = {
    (currProject, categoryMetaResponse, currentMeta) match {
      case (Some(project), Some(resp), Some(curMeta)) =>
        if (deltaCat.isEmpty)
          resp match {
            case CategoryMetaEquals(_, _) =>
              deltaCat = Some(context.system.actorOf(Category(curMeta, project)))

            case CategoryMetaDelta(delta) =>
              deltaCat = Some(context.system.actorOf(Category(delta, project)))
          }
      case _ =>
        log.debug("project or current category meta, or meta not compared yet")
    }

    if (!isSubcategoryCompared)
      for (dc <- deltaCat; metaDelta <- subMetaDelta) {
        for (m <- metaDelta)
          dc ! AddSubcategory(m)
      }

    if (isSubcategoryCompared)
      for (dc <- deltaCat; wfDelta <- workflowFromCategory; resp: AbstractCategoryMetaResponse <- categoryMetaResponse) {
        wfDelta match {
          case WorkflowFromCategoryDelta(wd) =>
            dc ! AddWorkflows(wd)
            receiver ! CategoryDelta(dc)
            context.stop(self)
          case WorkflowFromCategoryEquals(_, _) =>
            resp match {
              case CategoryMetaEquals(_, _) =>
                if (subMetaDelta.get.isEmpty)
                  receiver ! CategoryEquals(currentCat, prevCat)
                else
                  receiver ! CategoryDelta(dc)

              case CategoryMetaDelta(_) =>
                receiver ! CategoryDelta(dc)
            }

            context.stop(self)
        }
      }
  }
}
