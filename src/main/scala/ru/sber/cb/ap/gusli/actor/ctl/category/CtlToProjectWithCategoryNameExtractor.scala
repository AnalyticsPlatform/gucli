package ru.sber.cb.ap.gusli.actor.ctl.category

import java.nio.file.Paths

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.Project.GetCategoryRoot
import ru.sber.cb.ap.gusli.actor.{BaseActor, ctl}
import ru.sber.cb.ap.gusli.actor.ctl.CtlToProjectExtractor.ProjectExtracted
import ru.sber.cb.ap.gusli.actor.ctl.api.CtlToProjectConfigWithCategoryNames
import akka.pattern.ask
import akka.util.Timeout
import ru.sber.cb.ap.gusli.actor.core.Category.{AddSubcategory, CreateWorkflow, WorkflowCreated}
import ru.sber.cb.ap.gusli.actor.core.Workflow.BindEntity
import ru.sber.cb.ap.gusli.actor.core.dto.WorkflowDto
import ru.sber.cb.ap.gusli.actor.ctl.category.CategoryDtoDownloaderByName.{CategoryDtoResponse, ErrorWhileDownloading}
import ru.sber.cb.ap.gusli.actor.ctl.model.CategoryDto
import ru.sber.cb.ap.gusli.actor.ctl.project.ProjectCreator


object CtlToProjectWithCategoryNameExtractor {
  def apply(ctlConfig: CtlToProjectConfigWithCategoryNames, receiver: ActorRef) = Props(new CtlToProjectWithCategoryNameExtractor(ctlConfig, receiver))
}

class CtlToProjectWithCategoryNameExtractor(ctlConfig: CtlToProjectConfigWithCategoryNames, receiver: ActorRef) extends CtlToProject {
  override def preStart() = context.actorOf(CategoryDtoDownloaderByName(ctlConfig, self))
  
  // /cb/ap cb/ap/dev /cb/ap2
  override def receive: Receive = {
    case CategoryDtoResponse(m) =>
      context.actorOf(ProjectCreator(ctlConfig, m, self))

    case ErrorWhileDownloading(code, message) =>
      log.error(Console.RED + code.value + Console.WHITE)
      log.error(Console.RED + code.reason + Console.WHITE)
      log.error(Console.RED + message + Console.WHITE)
  }
  
  def checkFinish(project: ActorRef): Unit ={
    receiver ! ProjectExtracted(project)
  }
}

class CategoryFromDtoCreator(path:String, dto:CategoryDto, catRoot:ActorRef, receiver: ActorRef) extends BaseActor {
  override def preStart(): Unit = {
    
//    context.actorOf(Props(CategoriesFromPathCreator(path, catRoot, self)))
  }
  
  override def receive: Receive = {
    case WorkflowCreated(w) =>
//    case CategoriesFromPathCreated(cat)=>
//      dto match {
//        case CategoryDto(_,wfs)=>
//          for (w<-wfs)
//            cat ! CreateWorkflow(w.toMeta())
//      }
//
//    case WorkflowCreated(wf,wfMeta)=>
//      for (e<-findWf(wfMeta.name).entityIds)
//        wf ! BindEntity(e)
//
  }
  
//  def findWf(name:String): WorkflowDto ={
//
//  }
}

//class CategoriesFromPathCreator(path:String, catRoot:ActorRef, receiver: ActorRef) extends BaseActor {
//
//}


