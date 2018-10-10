package ru.sber.cb.ap.gusli.actor.core

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor._
import ru.sber.cb.ap.gusli.actor.core.Workflow.{GetWorkflowMeta, WorkflowMetaResponse}

import scala.collection.immutable.{HashMap, HashSet}

object Category {
  def apply(meta: CategoryMeta, project: ActorRef): Props = Props(new Category(meta, project))

  case class GetCategoryMeta(replyTo: Option[ActorRef] = None) extends Request

  case class GetProject(replyTo: Option[ActorRef] = None) extends Request

  case class AddSubcategory(meta: CategoryMeta, replyTo: Option[ActorRef] = None) extends Request

  case class GetSubcategories(replyTo: Option[ActorRef] = None) extends Request

  case class AddWorkflows(workflows: Set[ActorRef], replyTo: Option[ActorRef] = None) extends Request

  case class CreateWorkflow(meta: WorkflowMeta, replyTo: Option[ActorRef] = None) extends Request

  case class GetWorkflows(replyTo: Option[ActorRef] = None) extends Request

  //

  case class CategoryMetaResponse(meta: CategoryMeta) extends Response

  case class ProjectResponse(project: ActorRef) extends Response

  case class SubcategoryCreated(actorRef: ActorRef) extends ActorResponse

  case class SubcategorySet(actorSet: Set[ActorRef]) extends ActorSetResponse

  case class WorkflowCreated(actorRef: ActorRef) extends ActorResponse

  case class WorkflowSet(actorSet: Set[ActorRef]) extends ActorSetResponse

}

class Category(meta: CategoryMeta, project: ActorRef) extends BaseActor {

  import Category._

  private var subcategoresRegistry: HashMap[String, ActorRef] = HashMap.empty[String, ActorRef]
  private var workflowsRegistry: HashMap[String, ActorRef] = HashMap.empty[String, ActorRef]
  private var workflowsAwaitAdd: HashSet[ActorRef] = HashSet.empty

  override def receive: Receive = {
    case GetProject(sendTo) =>
      val replyTo = sendTo.getOrElse(sender)
      replyTo ! ProjectResponse(project)

    case AddWorkflows(workflows, _) =>
      for (wf <- workflows) {
        workflowsAwaitAdd = workflowsAwaitAdd + wf
        wf ! GetWorkflowMeta()
      }

    case WorkflowMetaResponse(m) =>
      if (workflowsRegistry.contains(m.name))
        throw new RuntimeException(s"Can't be add workflow:${m.name}, because it's already exists")

      val wf = sender()
      if (workflowsAwaitAdd.contains(wf)) {
        workflowsAwaitAdd = workflowsAwaitAdd - wf
        workflowsRegistry += m.name -> wf
      }
      else
        throw new RuntimeException(s"Unexpected workflow:$wf")

    case GetCategoryMeta(sendTo) =>
      val replyTo = sendTo.getOrElse(sender())
      replyTo ! CategoryMetaResponse(meta)

    case mess@AddSubcategory(m, sendTo) =>
      log.info("{}", mess)
      val replyTo = sendTo.getOrElse(sender())
      subcategoresRegistry.get(m.name) match {
        case Some(subcat) =>
          replyTo ! SubcategoryCreated(subcat)
          //replyTo ! subcat
        case None =>
          val subcat = context.actorOf(Category(m, project), m.name)
          subcategoresRegistry = subcategoresRegistry + (m.name -> subcat)
          replyTo ! SubcategoryCreated(subcat)
      }
    case GetSubcategories(sendTo) =>
      sendTo getOrElse sender ! SubcategorySet(subcategoresRegistry.values.toSet)

    case mess@CreateWorkflow(m, sendTo) =>
      log.info("{}", mess)
      val replyTo = sendTo.getOrElse(sender)
      val fromRegistry: Option[ActorRef] = workflowsRegistry.get(m.name)
      if (fromRegistry.isEmpty) {
        val newWorkflow = context.actorOf(Workflow(m, project))
        workflowsRegistry = workflowsRegistry + (m.name -> newWorkflow)
        replyTo ! WorkflowCreated(newWorkflow)
      }
      else {
        //todo: Why it's needs?
        if (fromRegistry.isDefined) {
          replyTo ! WorkflowCreated(fromRegistry.get)
        }
      }


    case GetWorkflows(sendTo) =>
      sendTo.getOrElse(sender) ! WorkflowSet(workflowsRegistry.values.toSet)
  }
}

trait CategoryMeta {
  def name: String

  // Content
  def sqlMap: Map[String, String]

  // Content
  def init: Map[String, String]

  def user: Option[String]

  def queue: Option[String]

  def grenkiVersion: Option[String]

  def params: Map[String, String]

  def stats: Set[Long]

  def entities: Set[Long]
}

case class CategoryMetaDefault(name: String,
                               sqlMap: Map[String, String] = Map.empty,
                               init: Map[String, String] = Map.empty,
                               user: Option[String] = None,
                               queue: Option[String] = None,
                               grenkiVersion: Option[String] = None,
                               params: Map[String, String] = Map.empty,
                               stats: Set[Long] = Set.empty,
                               entities: Set[Long] = Set.empty
                              ) extends CategoryMeta