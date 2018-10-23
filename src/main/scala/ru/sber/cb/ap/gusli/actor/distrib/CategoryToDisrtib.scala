package ru.sber.cb.ap.gusli.actor.distrib

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

import akka.actor.{ActorRef, Props}
import play.api.libs.json.Json
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}
import ru.sber.cb.ap.gusli.actor.core.dto.{CategoryDto, WorkflowDto}
import ru.sber.cb.ap.gusli.actor.ctl.model.{Category, Wf}
import ru.sber.cb.ap.gusli.actor.ctl.model.CtlJsonProtocol.{catWrites, wfWrites}
import FolderNames._
import ru.sber.cb.ap.gusli.actor.distrib.CategoryToDisrtib.CategoryWritten
import ru.sber.cb.ap.gusli.actor.distrib.category.CategoryHdfs

object CategoryToDisrtib {
  
  def apply(path: Path, fullCatName: String, cDto: CategoryDto, parentCDto: CategoryDto, receiver: ActorRef): Props =
    Props(new CategoryToDisrtib(path, fullCatName, cDto, parentCDto, receiver))
  
  case class CategoryWritten(cDto: CategoryDto) extends Response
  
}

/**
  *
  * @param artifactPath path to artifact folder
  * @param fullCatName name formated as "branch-root/.../grandParent/parent/this"
  * @param cDto CategoryDto
  * @param receiver whom to send CategoryWritten Response
  */
class CategoryToDisrtib(artifactPath: Path, fullCatName: String, cDto: CategoryDto, parentDto: CategoryDto, receiver: ActorRef) extends BaseActor {
  
  private val thisHdfsPath = artifactPath.resolve(apHdfs).resolve("user").resolve("__USER__").resolve(fullCatName)
  
  private val childrenCount = cDto.subcategories.size
  private var writtenChildrenCount = 0
  
  override def preStart(): Unit = {
    writeToDisk()
    writeChildren()
    checkFinish()
  }
  
  override def receive: Receive = {
    case CategoryWritten(_) =>
      writtenChildrenCount += 1
      checkFinish()
  }
  
  private def writeToDisk(): Unit = {
    writeCategory()
    writeWorkflows()
  }
  
  private def writeChildren(): Unit = {
    cDto.subcategories.foreach(c =>
      context.actorOf(CategoryToDisrtib(artifactPath, fullCatName + s"/${c.name}", c, cDto, self)))
  }
  
  private def writeCategory() = {
    val fileName = makeNormalJsonName(fullCatName)
    val writePath = artifactPath.resolve(apCtl).resolve(categories)resolve(if (cDto.isDeleted) delete else create)
    writeCategoryJson(fileName, writePath)
    CategoryHdfs(artifactPath.resolve(apHdfs), cDto, parentDto, fullCatName)
  }
  
  private def writeCategoryJson(fileName: String, writePath: Path) = {
    val jsEntity = Json.prettyPrint(Json.toJson(Category.namedCategory(fullCatName)))
    Files.createDirectories(writePath)
    Files.write(writePath.resolve(fileName), jsEntity.getBytes(StandardCharsets.UTF_8))
  }
  
  private def writeWorkflows(): Unit = {
    cDto.workflows.foreach{ wf =>
      val wfFileName = makeNormalJsonName(wf.name)
      val wfPath = artifactPath.resolve(apCtl).resolve(workflows).resolve(if (wf.isDeleted) delete else create)
      writeWorkflowJson(wfPath, wf, wfFileName)
      if (!wf.isDeleted) {
        writeEntityBind(wfPath, wf, wfFileName)
        writeStatBind(wfPath, wf, wfFileName)
      }
      writeWorkflowHdfs(wf)
    }
  }
  
  private def writeWorkflowJson(wfPath: Path, wf: WorkflowDto, fileName: String) = {
    val js = Json.prettyPrint(Json.toJson(Wf.fromDto(fullCatName, wf)))
    Files.createDirectories(wfPath)
    Files.write(wfPath.resolve(fileName), js.getBytes(StandardCharsets.UTF_8))
  }

  
  private def writeWorkflowHdfs(wDto: WorkflowDto): Unit = {
    val path = artifactPath.resolve(apHdfs).resolve("user").resolve("__USER__").resolve(fullCatName).resolve(makeNormalName(wDto.name))
    writeSqlMap(path, wDto.sqlMap)
    writeSqlInit(path, wDto.init)
    writeSql(path, wDto.sql, wDto.name)
  }
  
  private def writeEntityBind(path: Path, wDto: WorkflowDto, wfFileName: String) =
    writeSet(path, wDto.entities, wfFileName + ".bind")
  
  private def writeStatBind(path: Path, wDto: WorkflowDto, wfFileName: String) =
    writeSet(path, wDto.stats, wfFileName + ".stat")
  
  private def writeSet(path: Path, set: Set[Long], fileName: String) = {
    if (set.nonEmpty) {
      val s = set.mkString("\n")
      Files.write(path.resolve(fileName), s.getBytes(StandardCharsets.UTF_8))
    }
  }
  
  private def writeSqlMap(path: Path, m: Map[String, String]) = {
    if (m.nonEmpty) {
      Files.createDirectories(path)
      val content = m.values.mkString("\n")
      Files.write(path.resolve("sql.map"), content.getBytes(StandardCharsets.UTF_8))
    }
  }
  
  private def writeSqlInit(path: Path, m: Map[String, String]) = {
    if (m.nonEmpty) {
      Files.createDirectories(path)
      val content = m.values.mkString("; \n")
      Files.write(path.resolve("sql.init"), content.getBytes(StandardCharsets.UTF_8))
    }
  }
  
  private def writeSql(path: Path, m: Map[String, String], wfName: String) = {
    if (m.nonEmpty) {
      Files.createDirectories(path)
      val content = m.values.mkString("; \n")
      Files.write(path.resolve(wfName), content.getBytes(StandardCharsets.UTF_8))
    }
  }
  
  private def makeNormalJsonName(name: String): String = makeNormalName(name) + ".json"
  
  private def makeNormalName(name: String) = name.replace("/", "~").replace(":", "%3A").trim
  
  private def checkFinish(): Unit = if (writtenChildrenCount == childrenCount) finish()
  
  private def finish(): Unit = {
    receiver ! CategoryWritten(cDto)
    context.stop(self)
  }
}
