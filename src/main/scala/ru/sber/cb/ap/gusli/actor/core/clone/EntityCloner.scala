package ru.sber.cb.ap.gusli.actor.core.clone

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.Project.{EntityFound, EntityNotFound, FindEntity}
import ru.sber.cb.ap.gusli.actor.core.clone.EntityCloner.EntitiesCloneSuccessful
import ru.sber.cb.ap.gusli.actor.core.clone.EntityHierarchyCloner.EntityHierarchyCloneSuccessful
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}

object EntityCloner {
  def apply(fromRoot: ActorRef, toRoot: ActorRef, ids: Set[Long], receiver: ActorRef): Props = Props(new EntityCloner(fromRoot, toRoot, ids, receiver))

  object EntitiesCloneSuccessful extends Response

}

class EntityCloner(fromRoot: ActorRef, toRoot: ActorRef, var ids: Set[Long], receiver: ActorRef) extends BaseActor {
  var idsCount = -1

  override def preStart(): Unit = {
    if (ids.isEmpty) {
      receiver ! EntitiesCloneSuccessful
      context.stop(self)
    }
    else if (ids.nonEmpty) {
      idsCount = ids.size
      for (id <- ids.toList.sorted) {
        fromRoot ! FindEntity(id)
      }
    }
  }

  override def receive: Receive = {
    case EntityFound(m, e) =>
      context.actorOf(EntityHierarchyCloner(e, toRoot, self))
    case EntityHierarchyCloneSuccessful(_, _) =>
      idsCount -= 1
      checkFinish()

    case m:EntityNotFound=>
      println(m)
      println(sender())

  }

  def checkFinish(): Unit ={
    if (idsCount==0)
      receiver ! EntitiesCloneSuccessful
  }
}
