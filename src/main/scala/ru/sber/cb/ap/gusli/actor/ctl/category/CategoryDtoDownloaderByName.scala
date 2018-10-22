package ru.sber.cb.ap.gusli.actor.ctl.category

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import play.api.libs.json.{JsArray, JsValue, Json}
import ru.sber.cb.ap.gusli.actor.core.WorkflowMetaDefault
import ru.sber.cb.ap.gusli.actor.core.dto.WorkflowDto
import ru.sber.cb.ap.gusli.actor.ctl.RestGetter
import ru.sber.cb.ap.gusli.actor.ctl.api.CtlToProjectConfigWithCategoryNames
import ru.sber.cb.ap.gusli.actor.ctl.category.CategoryDtoDownloaderByName.{CategoryDtoResponse, ErrorWhileDownloading}
import ru.sber.cb.ap.gusli.actor.ctl.model.{CategoryDto, Param, Wf, WfExt}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}
import ru.sber.cb.ap.gusli.actor.ctl.model.CtlJsonProtocol._

import scala.io.Source

object CategoryDtoDownloaderByName {
  def apply(ctlConfig: CtlToProjectConfigWithCategoryNames, receiver: ActorRef) = Props(new CategoryDtoDownloaderByName(ctlConfig, receiver))
  
  case class CategoryDtoResponse(mapWithNames: Map[String, Set[WorkflowDto]]) extends Response
  
  case class ErrorWhileDownloading(code: StatusCode, message: String) extends Response
}

class CategoryDtoDownloaderByName(ctlConfig: CtlToProjectConfigWithCategoryNames, receiver:ActorRef) extends BaseActor {
  
  var workflowResponses = 0
  
  override def preStart(): Unit = {
    //TODO  context.actorOf(RestGetter(ctlConfig.ctlUrl + suffixToAllCategories))
    val m = Source.fromURL("hide").mkString
    self ! RestGetter.HttpBody(m)
  }
  
  override def receive: Receive = {
    case RestGetter.HttpBody(m) =>
      val jsArr = Json.parse(m).as[JsArray]
      val cats = jsArr.value.filter(c => isCatInList(c))
      if (cats.size > ctlConfig.categories.size)
        receiver ! ErrorWhileDownloading(StatusCodes.MultipleChoices, s"More then one category exists with same names")
      else {
        val catNamesAndIds = cats.map(c => ((c \ "name").as[String], (c \ "id").as[Long])).toMap
        
        val wfs: Map[String, Set[WorkflowDto]] = for ((name, id) <- catNamesAndIds) yield {
          //TODO  context.actorOf(RestGetter(ctlConfig.ctlUrl + suffixToAllWfCategories.replace("category", id)))
          //Haha =)
          val strWfs = Source.fromURL("hide" + id).mkString
          val jsWfs = Json.parse(strWfs).as[JsArray]
          (name, jsWfs.value.filter(v => !(v \ "deleted").as[Boolean]).map(js => makeWfDtoFromJs(js)).toSet)
        }
        
        receiver ! CategoryDtoResponse(wfs)
      }

    case RestGetter.HttpBodyError(code) => receiver ! ErrorWhileDownloading(code, "")
  }
  
  private def isCatInList(c: JsValue) = {
    ctlConfig.categories.contains(nameOf(c))
  }
  
  private def nameOf(category: JsValue) = (category \ "name").as[String]
  
  private def makeWfDtoFromJs(jsWf: JsValue) = {
    val objWf = Json.fromJson[Wf](jsWf).get
    //TODO norm url
    val strWfExt = Source.fromURL(s"hide ${objWf.id}/export").mkString
    val jsWfExt = (Json.parse(strWfExt) \ "wfExt").get
    val objWfExt = Json.fromJson[WfExt](jsWfExt).get
    val wfMeta = WorkflowMetaDefault(
      name = objWf.name,
      sql = Map((objWf.name + ".sql") -> downloadSql),
      sqlMap = Map("sql.map" -> downloadSqlMap),
      init = Map("init.sql" -> downloadSqlInit),
      user = Some(findParam(objWf.param, "user")),
      queue = Some(findParam(objWf.param, "queue")),
      grenkiVersion = Some(globalGrenkiVersion),
      params = Map("param" -> makeParams(objWf.param)),
      stats = objWfExt.connectedStats.map(_.toLong).toSet
    )
    WorkflowDto(wfMeta, objWfExt.connectedEntities.map(_.toLong).toSet)
  }
  
  private val downloadSql = "Implement method"
  private val downloadSqlMap = "Implement method"
  private val downloadSqlInit = "Implement method"
  private def findParam(wfParams: Seq[Param], param: String) = "Implement method"
  private val globalGrenkiVersion = "Implement method"
  private def makeParams(wfParams: Seq[Param]) = "Implement method, exclude other params"
}