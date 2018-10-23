package ru.sber.cb.ap.gusli.actor.distrib

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

import akka.actor.{ActorRef, Props}
import play.api.libs.json.Json
import ru.sber.cb.ap.gusli.actor.core.dto.EntityDto
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}
import ru.sber.cb.ap.gusli.actor.ctl.model.CtlJsonProtocol.entityWrites
import ru.sber.cb.ap.gusli.actor.ctl.model._
import ru.sber.cb.ap.gusli.actor.distrib.EntityToDistrib.EntityWritten

object EntityToDistrib {
  
  def apply(path: Path, eDto: EntityDto, receiver: ActorRef): Props = Props(new EntityToDistrib(path, eDto, receiver))
  
  case class EntityWritten(eDto: EntityDto) extends Response
}

class EntityToDistrib(entCreatePath: Path, eDto: EntityDto, receiver: ActorRef) extends BaseActor {
  
  private val childrenCound = eDto.children.size
  private var writtenChildrenCount = 0
  
  override def preStart(): Unit = {
    writeToDisk()
    eDto.children.foreach(e => context.actorOf(EntityToDistrib(entCreatePath, e, self)))
    checkFinish()
  }
  
  override def receive: Receive = {
    case EntityWritten(_) =>
      writtenChildrenCount += 1
      checkFinish()
  }
  
  private def writeToDisk(): Unit = {
    val fileName = eDto.id.toString + ".json"
    val jsEntity = Json.prettyPrint(Json.toJson(Entity.fromDto(eDto: EntityDto)))
    Files.write(entCreatePath.resolve(fileName), jsEntity.getBytes(StandardCharsets.UTF_8))
  }
  
  private def checkFinish(): Unit = if (writtenChildrenCount == childrenCound) finish()
  
  private def finish(): Unit = {
    receiver ! EntityWritten(eDto)
    context.stop(self)
  }
}
