package ru.sber.cb.ap.gusli.actor.core.diff

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.Category.{CategoryMetaResponse, GetCategoryMeta}
import ru.sber.cb.ap.gusli.actor.core.diff.CategoryDiffer.CategoryDifferResponse
import ru.sber.cb.ap.gusli.actor.core.{CategoryMeta, Project, ProjectMeta}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}

object ProjectDiffer {
  def apply(currentProject: ActorRef, prevProject: ActorRef, receiver: ActorRef): Props = Props(new ProjectDiffer(currentProject, prevProject, receiver))

  abstract class ProjectDifferResponse extends Response

  case class ProjectEquals(currentProject: ActorRef, prevProject: ActorRef) extends ProjectDifferResponse

  case class ProjectDelta(deltaProject: ActorRef) extends ProjectDifferResponse

}


class ProjectDiffer(currentProject: ActorRef, prevProject: ActorRef, receiver: ActorRef) extends BaseActor {

  import ProjectDiffer._
  import ru.sber.cb.ap.gusli.actor.core.Project._

  var currentRootCat: Option[ActorRef] = None
  var prevRootCat: Option[ActorRef] = None
  var projectDiff: Option[ActorRef] = None
  var currentProjectMeta: Option[ProjectMeta] = None
  var currentCategoryMeta: Option[CategoryMeta] = None
  var categoryDiff: Option[Response] = None

  override def preStart(): Unit = {
    currentProject ! GetProjectMeta()

    currentProject ! GetCategoryRoot()

    prevProject ! GetCategoryRoot()
  }

  override def receive: Receive = {
    case CategoryRoot(root) =>
      if (sender == currentProject) {
        currentRootCat = Some(root)
        root ! GetCategoryMeta()
      }
      else if (sender == prevProject)
        prevRootCat = Some(root)
      else throw new RuntimeException(s"Unexpectable sender $sender")

      for (curr <- currentRootCat; prev <- prevRootCat) {
        context.actorOf(CategoryDiffer(curr, prev, self))
      }

    case ProjectMetaResponse(meta) =>
      currentProjectMeta = Some(meta)
      createDiffProject()
    case CategoryMetaResponse(meta) =>
      currentCategoryMeta = Some(meta)
      createDiffProject()

    case catDiff: CategoryDifferResponse =>
      categoryDiff = Some(catDiff)
      copyCategoryDelta()
  }

  def createDiffProject(): Unit =
    for (pMeta <- currentProjectMeta; catMeta <- currentCategoryMeta) {
      projectDiff = Some(context.system.actorOf(Project(pMeta, catMeta)))
      copyCategoryDelta()
    }

  def copyCategoryDelta(): Unit = {
    for (proj <- projectDiff; catDiff <- categoryDiff) {
      receiver ! ProjectDelta(proj)
    }
  }
}

