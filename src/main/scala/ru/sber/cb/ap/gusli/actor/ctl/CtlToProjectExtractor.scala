package ru.sber.cb.ap.gusli.actor.ctl

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}
import ru.sber.cb.ap.gusli.actor.ctl.CtlToProjectExtractor.ProjectExtracted
import ru.sber.cb.ap.gusli.actor.ctl.api._
import ru.sber.cb.ap.gusli.actor.ctl.category.CtlToProjectWithCategoryNameExtractor

object CtlToProjectExtractor {
  def apply(ctlConfig: CtlToProjectConfig, receiver: ActorRef): Props = Props(new CtlToProjectExtractor(ctlConfig, receiver))
  
  case class ProjectExtracted(project: ActorRef) extends Response
}

class CtlToProjectExtractor(ctlConfig: CtlToProjectConfig, receiver: ActorRef) extends BaseActor {
  
  override def preStart() = {
    ctlConfig match {
      case c: CtlToProjectConfigWithCategoryNames => context.actorOf(CtlToProjectWithCategoryNameExtractor(c, self))
      case c: CtlToProjectConfigFilter => context.actorOf(CtlToProjectWithFilterExtractor(c, self))
    }
    
    //val categories=rest.getAllCategory().filterBy(x=.x.startWith(ctl.query))
    //val categories=rest.getAllCategory().filterBy(x=.x.startWith(ctl.query))
    //for (c<-categories)
    //context.actorOf(CategoryCreator(c))
    //for (wf<-rest.getWfs(c))
    //context.actorOf(CategoryCreator(c,wf))
    //val sub=project.rootCategory.createSubcategory(c.toMeta)
    //sub.creteWorkflow(wf.toMeta)

    
  }
  
  override def receive = {
    case ProjectExtracted(project) => receiver ! ProjectExtracted(project)
  
  }
  

}






object CtlToProjectWithFilterExtractor {
  def apply(ctlConfig: CtlToProjectConfigFilter, receiver: ActorRef) = Props(new CtlToProjectWithFilterExtractor(ctlConfig, receiver))
}
class CtlToProjectWithFilterExtractor(ctlConfig: CtlToProjectConfigFilter, receiver:ActorRef) extends BaseActor {
  override def receive: Receive = ???
}

