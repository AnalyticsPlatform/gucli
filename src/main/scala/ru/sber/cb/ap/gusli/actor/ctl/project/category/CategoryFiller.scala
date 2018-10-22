package ru.sber.cb.ap.gusli.actor.ctl.project.category

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}
import ru.sber.cb.ap.gusli.actor.core.dto.WorkflowDto
import ru.sber.cb.ap.gusli.actor.ctl.project.category.CategoryFiller.CategoryFilled

object CategoryFiller {
  def apply(parentCategory: ActorRef, ctlCatName: String, workflowDtoSet: Set[WorkflowDto], receiver: ActorRef): Props = Props(new CategoryFiller(parentCategory, ctlCatName, workflowDtoSet, receiver))

  case class CategoryFilled(category: ActorRef) extends Response
}

class CategoryFiller(parentCategory: ActorRef, catName: String, workflowDtoSet: Set[WorkflowDto], receiver: ActorRef) extends BaseActor {
  private var filledCount = 0
  
  override def preStart(): Unit = receiver ! CategoryFilled(parentCategory)
  
  override def receive: Receive = {
    case CategoryFilled(category) =>
  }
}
