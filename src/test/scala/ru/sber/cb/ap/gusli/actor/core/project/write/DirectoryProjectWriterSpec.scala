package ru.sber.cb.ap.gusli.actor.core.project.write

import java.nio.file.Paths

import akka.actor.ActorRef
import org.scalatest
import ru.sber.cb.ap.gusli.actor.core.ActorBaseTest
import ru.sber.cb.ap.gusli.actor.projects.read.DirectoryProjectReader
import ru.sber.cb.ap.gusli.actor.projects.read.DirectoryProjectReader.{ProjectReaded, ReadProject}
import ru.sber.cb.ap.gusli.actor.projects.read.util.FileContentReader
import ru.sber.cb.ap.gusli.actor.projects.write.ProjectWriter
import ru.sber.cb.ap.gusli.actor.projects.write.ProjectWriter.{ProjectWrited, WriteProject}

import scala.concurrent.duration._

class DirectoryProjectWriterSpec extends ActorBaseTest("DirectoryProjectSpec") {
  val correctPath = Paths.get("./src/test/resources/project_test-2")
  val writePath = Paths.get("./target/")
  val directoryProjectReader: ActorRef = system.actorOf(DirectoryProjectReader(correctPath))
  
  "Directory project writer" when {
    var project: ActorRef = null
  
    "projectReader reads project" should {
      "wait until project haven't been readed" in {
        directoryProjectReader ! ReadProject()
        expectMsgPF(10 seconds) {
          case ProjectReaded(inputProject) => project = inputProject
        }
      }
    }
  
    "receive WriteProject" should {
      "send back ProjectWrited()" in {
        val directoryProjectWriter: ActorRef = system.actorOf(ProjectWriter(project, writePath))
        directoryProjectWriter ! WriteProject()
        expectMsg(10 seconds, ProjectWrited())
      }
  
      "and project folder exists" in {
        val projFolder = writePath.resolve("project_test-2")
        assert(projFolder.toFile.exists)
        val pathLine = Paths.get("./target/project_test-2/entity/105000000 entity-root/105060000 entity-parent/105067000 entity-children")
        assert(pathLine.toFile.exists)
        assert(pathLine.toFile.isDirectory)
        assert(pathLine.toFile.listFiles.length == 4)
      }

      "and the category inside the project exists" in {
        var pathCategFolder = Paths.get("./target/project_test-2/category/cb/ap/rb")
        assert(pathCategFolder.toFile.exists)
        assert(pathCategFolder.toFile.isDirectory)
        assert(pathCategFolder.toFile.listFiles.length == 6)
        pathCategFolder = pathCategFolder.resolve("wf-rb-vek555/init.hql")
        val pathContent = FileContentReader.readFileContent(pathCategFolder)
        println(pathContent)
      }
    }
  }
}