package ru.sber.cb.ap.gusli.actor.ctl

import java.awt.Color

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import ru.sber.cb.ap.gusli.actor.ctl.RestGetter.Response

import scala.concurrent.{Await, Future}

object RestGetter {
  def apply(uri: String, replyTo: ActorRef): Props = Props(new RestGetter(uri: String, replyTo: ActorRef))
  
  case class Response(mes: ResponseEntity)
}

class RestGetter(url: String, replyTo: ActorRef) extends Actor
  with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  override def preStart() = {
    http.singleRequest(HttpRequest(uri = this.url))
    .pipeTo(self)
  }

  def receive = {
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        log.info("Got response, body: " + body.utf8String)
      }
      replyTo ! Response(entity)
    case resp @ HttpResponse(code, _, _, _) =>
      log.error("Request failed, response code: " + code)
      resp.discardEntityBytes()
  }

}