package ru.sber.cb.ap.gusli.actor.distrib

import java.nio.file.{Files, Path}

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.diff.ProjectDiffer.{ProjectDelta, ProjectEquals}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}
import FolderNames._
import ru.sber.cb.ap.gusli.actor.core.dto.CategoryDto
import ru.sber.cb.ap.gusli.actor.distrib.CategoryToDisrtib.CategoryWritten
import ru.sber.cb.ap.gusli.actor.distrib.EntityToDistrib.EntityWritten
import ru.sber.cb.ap.gusli.actor.distrib.ProjectToDistrib.DistribWasMade

object ProjectToDistrib {
  
  def apply(path: Path, name: String, receiver: ActorRef): Props = Props(new ProjectToDistrib(path, name, receiver))
  
  case class DistribWasMade(path: Path) extends Response
}

class ProjectToDistrib(path: Path, name: String, receiver: ActorRef) extends BaseActor {
  var artifactPath: Path = _
  var catWritten = false
  var entWritten = false
  override def preStart(): Unit = {
    createDistDirs()
    createDeployParam()
  }
  
  override def receive: Receive = {
    case ProjectEquals(curr, prev) => receiver ! ProjectEquals(curr, prev)
    case ProjectDelta(p) =>
      val entPath = artifactPath.resolve(apCtl).resolve(entities).resolve(create)
      val root = CategoryDto("root")
      p.categoryRoot.subcategories.foreach(s => context.actorOf(CategoryToDisrtib(artifactPath, s.name, s, root, self)))
      p.entityRoot.children.foreach(e => context.actorOf(EntityToDistrib(entPath, p.entityRoot, self)))
    case CategoryWritten(_) =>
      catWritten = true
      checkFinish()
    case EntityWritten(_) =>
      entWritten = true
      checkFinish()
  }
  
  private def checkFinish() = if (entWritten & catWritten) finish()
  
  private def finish() = {
    receiver ! DistribWasMade(path)
    context.stop(self)
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
  
  private def createDeployParam(): Unit = {
    case class DeployParam(ctl_deploy_param: Map[String, String])
    import ru.sber.cb.ap.gusli.actor.projects.yamlfiles._
    
    val fileName = path.resolve(name).resolve(apCtl).resolve("deploy_param.yml")
    val deployParam = DeployParam(Map("oozie.wf.application.path" -> "hdfs:///user/{{ TECH_USERNAME }}/app/grenki-0.1/workflow.xml"))
    writeFieldsToFile(fileName, deployParam)
  }
}
