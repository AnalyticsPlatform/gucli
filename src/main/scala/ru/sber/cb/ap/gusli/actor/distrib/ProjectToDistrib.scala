package ru.sber.cb.ap.gusli.actor.distrib

import java.nio.file.{Files, Path}

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.diff.ProjectDiffer.{ProjectDelta, ProjectEquals}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}
import FolderNames._

object ProjectToDistrib {
  
  def apply(path: Path, name: String, receiver: ActorRef): Props = Props(new ProjectToDistrib(path, name, receiver))
  
  case class DistribWasMade(path: Path) extends Response
}

class ProjectToDistrib(path: Path, name: String, receiver: ActorRef) extends BaseActor {
  var artifactPath: Path = _
  override def preStart(): Unit = createDistDirs()
  
  override def receive: Receive = {
    case ProjectEquals(curr, prev) => receiver ! ProjectEquals(curr, prev)
    case ProjectDelta(p) =>
      val entPath = artifactPath.resolve(apCtl).resolve(entities).resolve(create)
      p.categoryRoot.subcategories.foreach(s => context.actorOf(CategoryToDisrtib(artifactPath, s.name, s, self)))
      p.entityRoot.children.foreach(e => context.actorOf(EntityToDistrib(entPath, p.entityRoot, self)))
  }
  
  private def createDistDirs(): Unit = {
    artifactPath = path.resolve(name)
    val apHdfsPath = artifactPath.resolve(apHdfs)
    val apCtlPath = artifactPath.resolve(apCtl)
    val apCtlFolders = Set(categories, entities, workflows)
    val innerFolders = Set(create, change, delete)
    
    Files.createDirectories(apHdfsPath)
    apCtlFolders.foreach{ f =>
      innerFolders.foreach { inner =>
        Files.createDirectories(apCtlPath.resolve(f).resolve(inner))
      }
    }
  }
}
