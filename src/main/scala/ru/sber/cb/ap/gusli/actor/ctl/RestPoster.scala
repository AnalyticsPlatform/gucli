package ru.sber.cb.ap.gusli.actor.ctl

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import ru.sber.cb.ap.gusli.actor.Response
import ru.sber.cb.ap.gusli.actor.ctl.RestPoster.HttpBody

object RestPoster {
  def apply(url: String, jsBody: String = "", replyTo: ActorRef): Props = Props(new RestPoster(url, jsBody, replyTo))
  
  case class HttpBody(body: String) extends Response
  
  case class HttpBodyError(code: StatusCode) extends Response
}

class RestPoster(url: String, jsBody: String, replyTo: ActorRef) extends Actor with ActorLogging {
  
  import akka.pattern.pipe
  import context.dispatcher
  
  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  
  val http = Http(context.system)
  
  override def preStart() = {
    http.singleRequest(
      HttpRequest(
        uri = this.url,
        method = HttpMethods.POST,
        entity = HttpEntity(ContentTypes.`application/json`, jsBody.getBytes())
      )
    ).pipeTo(self)
  }
  
  override def receive = {
    case HttpResponse(StatusCodes.Created, headers, entity, _) =>
      val v = Unmarshaller.stringUnmarshaller(entity)
          .value.get.get
      replyTo ! HttpBody(v)

     case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      val v = Unmarshaller.stringUnmarshaller(entity)
          .value.get.get
      replyTo ! HttpBody(v)

    case resp @ HttpResponse(code, _, _, _) =>
      log.error("Request failed, response code: " + code)
      resp.discardEntityBytes()
  }
}
