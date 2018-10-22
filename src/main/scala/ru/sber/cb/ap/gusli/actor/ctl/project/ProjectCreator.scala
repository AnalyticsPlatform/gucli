package ru.sber.cb.ap.gusli.actor.ctl.project

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.Project.{CategoryRoot, EntityRoot, GetCategoryRoot, GetEntityRoot}
import ru.sber.cb.ap.gusli.actor.core.{Project, ProjectMetaDefault}
import ru.sber.cb.ap.gusli.actor.core.dto.WorkflowDto
import ru.sber.cb.ap.gusli.actor.ctl.api.{CtlToProjectConfigWithCategoryNames, EnvTypes}
import ru.sber.cb.ap.gusli.actor.ctl.project.ProjectCreator.ProjectCreated
import ru.sber.cb.ap.gusli.actor.ctl.project.category.CategoryFiller
import ru.sber.cb.ap.gusli.actor.ctl.project.category.CategoryFiller.CategoryFilled
import ru.sber.cb.ap.gusli.actor.ctl.project.entity.EntityFiller.EntityFilled
import ru.sber.cb.ap.gusli.actor.ctl.project.entity.{EntityFiller, EntityLeaf, EntityTree}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}

object ProjectCreator {
  
  def apply(ctlConfig: CtlToProjectConfigWithCategoryNames, catNamesWithWorkflows: Map[String, Set[WorkflowDto]], receiver: ActorRef): Props = Props(new ProjectCreator(ctlConfig, catNamesWithWorkflows, receiver))
  
  case class ProjectCreated(project: ActorRef) extends Response
}

class ProjectCreator(ctlConfig: CtlToProjectConfigWithCategoryNames, catNamesWithWorkflows: Map[String, Set[WorkflowDto]], receiver: ActorRef) extends BaseActor {
  private var entityTree: EntityTree = _
  private var project: ActorRef = _
  private var filledCatCount = 0
  
  override def preStart(): Unit = extractEntities()
  
  private def extractEntities(): Unit = {
    createEntityTree()
    project = context.system.actorOf(Project(ProjectMetaDefault(makeProjectName)))
    project ! GetEntityRoot()
  }
  
  override def receive: Receive = {
    case EntityRoot(entityRoot) =>
      entityTree.root.children.foreach(leaf => addEntityToProject(entityRoot, leaf))

    case EntityFilled(_, _) => project ! GetCategoryRoot()

    case CategoryRoot(rootCat) =>
      for ((ctlCatName, wfDtoSet) <- catNamesWithWorkflows) {
        context.actorOf(CategoryFiller(rootCat, ctlCatName, wfDtoSet, self))
      }

    case CategoryFilled(cRoot) =>
      filledCatCount += 1
      checkFinish()
  }
  
  private def createEntityTree() = {
    val entities = catNamesWithWorkflows.values.flatten.flatMap(w => w.entities).toSet
    entityTree = EntityTree(entities, ctlConfig)
  }
  
  private def makeProjectName = {
    ctlConfig.env match {
      case a@EnvTypes.Dev => "project-dev"
      case a@EnvTypes.St => "project-st"
    }
  }
  
  private def addEntityToProject(entityRoot: ActorRef, leaf: EntityLeaf) = {
    context.actorOf(EntityFiller(entityRoot, leaf, self))
  }
  
  def checkFinish(): Unit = if (filledCatCount == catNamesWithWorkflows.size)
    finish()
  
  def finish() = {
    receiver ! ProjectCreated(project)
    context.stop(self)
  }
}
