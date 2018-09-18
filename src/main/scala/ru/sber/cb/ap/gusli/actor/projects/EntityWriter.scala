package ru.sber.cb.ap.gusli.actor.projects

import java.nio.file.Path

import akka.actor.Props
import ru.sber.cb.ap.gusli.actor.BaseActor
import ru.sber.cb.ap.gusli.actor.core.Category.GetCategoryMeta
import ru.sber.cb.ap.gusli.actor.core.Entity.{ChildrenEntityList, EntityMetaResponse, GetEntityMeta}
import ru.sber.cb.ap.gusli.actor.core.Project.EntityRoot
import ru.sber.cb.ap.gusli.actor.core.{Entity, EntityMeta}

class EntityWriter(path:Path, parentMeta:EntityMeta) extends BaseActor{

  var meta:EntityMeta  = _

  override def receive: Receive = {

    case EntityRoot(entityRootActorRef) =>
      entityRootActorRef ! GetEntityMeta(Some(context.self))

    case EntityMetaResponse(entityMeta) =>
      meta = entityMeta
      sender ! Entity.GetChildren(Some(context.self))

    case ChildrenEntityList(actorList) =>
      val entityFolderPath = MetaToHDD.writeEntityMetaToPath(meta, path, parentMeta, actorList.nonEmpty)
      for (subentity <- actorList){
        val entityWriterActorRef = context actorOf EntityWriter(entityFolderPath, meta)
        subentity ! GetCategoryMeta(Some(entityWriterActorRef))
      }
  }
}

object EntityWriter {
  def apply(path:Path,parentMeta:EntityMeta): Props = Props(new EntityWriter(path,parentMeta:EntityMeta))
}
