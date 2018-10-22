//package ru.sber.cb.ap.gusli.actor.ctl.category
//
//import akka.actor.{Actor, ActorLogging}
//import ru.sber.cb.ap.gusli.actor.ctl.RestGetter
//import ru.sber.cb.ap.gusli.actor.ctl.RestGetter.HttpBody
//import ru.sber.cb.ap.gusli.actor.ctl.model.Models
//import ru.sber.cb.ap.gusli.actor.ctl._
//
//object CategoryDownloaderWithChildren {
//
//}
//
//class CategoryDownloaderWithChildren(name: String, ctlUrl: String) extends Actor with ActorLogging {
//
//
//  override def preStart() = {
//    context.actorOf(RestGetter(ctlUrl + getCategoriesFromConfig, self))
//  }
//
//  override def receive: Receive = {
//    case RestGetter.HttpBody(message) =>
//      val m = transformToMeta(message, Models.Category)
//      getParents(ctlUrl)
//      fillProject()
//      sendToChildren()
//  }
//}
