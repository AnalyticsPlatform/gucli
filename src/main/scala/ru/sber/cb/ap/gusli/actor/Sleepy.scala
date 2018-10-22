package ru.sber.cb.ap.gusli.actor

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.Sleepy.WakeUp

import concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Sleepy {
  def apply(durationSec: Int = 1, replyTo: ActorRef): Props = Props(new Sleepy(durationSec, replyTo))
  
  case class WakeUp() extends Response
  
}

class Sleepy(durationSec: Int, replyTo: ActorRef) extends BaseActor {
  
  override def preStart(): Unit = {
    context.system.scheduler.scheduleOnce(durationSec seconds) {
      replyTo ! WakeUp()
    }
  }
  
  override def receive: Receive = {
    case WakeUp() =>
  }
}