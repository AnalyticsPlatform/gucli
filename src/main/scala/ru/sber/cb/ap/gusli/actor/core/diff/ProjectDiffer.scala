package ru.sber.cb.ap.gusli.actor.core.diff

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.Category._
import ru.sber.cb.ap.gusli.actor.core.copier.CategoryCopier
import ru.sber.cb.ap.gusli.actor.core.copier.CategoryCopier.CopiedSuccessfully
import ru.sber.cb.ap.gusli.actor.core.diff.CategoryDiffer.{CategoryDelta, CategoryEquals}
import ru.sber.cb.ap.gusli.actor.core.{CategoryMeta, Project, ProjectMeta}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}
import akka.pattern.ask
import akka.util.Timeout

import concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object ProjectDiffer {
  def apply(currentProject: ActorRef, prevProject: ActorRef, receiver: ActorRef): Props = Props(new ProjectDiffer(currentProject, prevProject, receiver))

  abstract class ProjectDifferResponse extends Response

  case class ProjectEquals(currentProject: ActorRef, prevProject: ActorRef) extends ProjectDifferResponse

  case class ProjectDelta(deltaProject: ActorRef) extends ProjectDifferResponse

}


class ProjectDiffer(currentProject: ActorRef, prevProject: ActorRef, receiver: ActorRef) extends BaseActor {
  implicit val timeout = Timeout(5 hour)
  import ProjectDiffer._
  import ru.sber.cb.ap.gusli.actor.core.Project._

  var currentRootCat: Option[ActorRef] = None
  var prevRootCat: Option[ActorRef] = None
  var projectDiff: Option[ActorRef] = None
  var currentProjectMeta: Option[ProjectMeta] = None
  var currentCategoryMeta: Option[CategoryMeta] = None
  var rootCatDiff: Option[ActorRef] = None

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

    case CategoryEquals(_, _) =>
      receiver ! ProjectEquals(currentProject, prevProject)
      context.stop(self)

    case CategoryDelta(catDiff) =>
      rootCatDiff = Some(catDiff)
      copyCategoryDelta()

    case CopiedSuccessfully() =>
      receiver ! ProjectDelta(projectDiff.get)
      context.stop(self)
  }

  def createDiffProject(): Unit =
    for (pMeta <- currentProjectMeta; catMeta <- currentCategoryMeta) {
      projectDiff = Some(context.system.actorOf(Project(pMeta, catMeta)))
      copyCategoryDelta()
    }

  def copyCategoryDelta(): Unit = {
    var set:Set[ActorRef]=null
    for (proj <- projectDiff; prevRoot <- prevRootCat; root <- rootCatDiff) {
      (root ? GetSubcategories()).map(x=>x.asInstanceOf[SubcategorySet]).map(
        x=>{
          log.debug("{}",x)
          set=x.actorSet
          (x.actorSet.head ? GetSubcategories()).map(x=>{
            log.debug("{}",x)
          })
        }
      )
      (root ? GetCategoryMeta()).map(
        x=>{
          log.debug("{}",x)
        }
      )
      context.actorOf(CategoryCopier(toProject = proj, fromProject = prevProject, toCategory = root, fromCategory = prevRoot, receiver = self))
      for (c<-set) {
        (c ? GetCategoryMeta()).map(x=>x.asInstanceOf[CategoryMetaResponse]).map(
          x=>{
            log.debug("{}",x)
            (root ? AddSubcategory(x.meta)).map(x=>{
              log.debug("{}",x)
            })
          }
        )
      }
    }
  }
}

