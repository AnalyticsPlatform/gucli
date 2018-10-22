package ru.sber.cb.ap.gusli.actor.ctl.category

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.BaseActor
import ru.sber.cb.ap.gusli.actor.ctl.api.CtlToProjectConfigFilter

object CtlToProjectWithFilterExtractor {
  def apply(ctlConfig: CtlToProjectConfigFilter, receiver: ActorRef) = Props(new CtlToProjectWithFilterExtractor(ctlConfig, receiver))
}
class CtlToProjectWithFilterExtractor(ctlConfig: CtlToProjectConfigFilter, receiver:ActorRef) extends BaseActor {
  
  override def preStart(): Unit = ???
  
  override def receive: Receive = ???
}