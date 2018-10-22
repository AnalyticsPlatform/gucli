package ru.sber.cb.ap.gusli.actor.ctl

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCode, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import ru.sber.cb.ap.gusli.actor.{Response, Sleepy}
import ru.sber.cb.ap.gusli.actor.Sleepy.WakeUp
import ru.sber.cb.ap.gusli.actor.ctl.RestGetter.{HttpBody, HttpBodyError}

object RestGetter {
  def apply(url: String, replyTo: ActorRef): Props = Props(new RestGetter(url, replyTo))
  
  case class HttpBody(body: String) extends Response
  
  case class HttpBodyError(code: StatusCode) extends Response
}

/**
  * The actor class having single implementation - preStart(). Send get-request to <b>url</b>, and then send response as string to <b>reply</b>
  * @param url
  * @param replyTo
  */
class RestGetter(url: String, replyTo: ActorRef) extends Actor with ActorLogging {
  
  var attemptCount = 0
  
  import akka.pattern.pipe
  import context.dispatcher
  
  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  
  val http = Http(context.system)
  
  override def preStart() = tryRequest()
  
  override def receive = {
    case WakeUp() => preStart()
    
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      println(Console.GREEN + StatusCodes.OK + Console.WHITE)
      println(Console.YELLOW + headers + Console.WHITE)
      println(Console.BLUE + entity + Console.WHITE)
      
      val bodyOption = Unmarshaller.stringUnmarshaller(entity).value
      if (bodyOption.isDefined) {
        replyTo ! HttpBody(bodyOption.get.get)
        context.stop(self)
      }
      else
        context.actorOf(Sleepy(attemptCount, self))

    case resp @ HttpResponse(code, _, _, _) =>
      log.error("Request failed, response code: " + code)
      resp.discardEntityBytes()
      replyTo ! HttpBodyError(code)
      context.stop(self)
  }
  
  private def tryRequest() = {
    attemptCount += 1
    if (attemptCount <= 5) http.singleRequest(HttpRequest(uri = this.url)).pipeTo(self)
    else {
      replyTo ! HttpBodyError(StatusCodes.RequestTimeout)
      context.stop(self)
    }
  }
}
