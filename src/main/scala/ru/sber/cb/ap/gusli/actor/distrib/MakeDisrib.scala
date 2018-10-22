package ru.sber.cb.ap.gusli.actor.distrib

import java.nio.file.{Files, Path}

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.diff.ProjectDiffer.{ProjectDelta, ProjectEquals}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}

object MakeDisrib {
  
  def apply(path: Path, name: String, receiver: ActorRef): Props = Props(new MakeDisrib(path, name, receiver))
  
  case class DistribWasMade(path: Path) extends Response
}

class MakeDisrib(path: Path, name: String, receiver: ActorRef) extends BaseActor {
  
  override def preStart(): Unit = createDistDirs()
  
  override def receive: Receive = {
    case ProjectEquals(curr, prev) => receiver ! ProjectEquals(curr, prev)
    case ProjectDelta(p) => p.categoryRoot
  }
  
  private def createDistDirs(): Unit = {
    import FolderNames._
    val artifactPath = path.resolve(name)
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
