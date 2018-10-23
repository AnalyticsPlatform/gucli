package ru.sber.cb.ap.gusli.actor.distrib

import java.nio.file.Paths

import akka.actor.ActorRef
import ru.sber.cb.ap.gusli.actor.core.ActorBaseTest
import ru.sber.cb.ap.gusli.actor.core.diff.ProjectDiffer
import ru.sber.cb.ap.gusli.actor.distrib.ProjectToDistrib.DistribWasMade
import ru.sber.cb.ap.gusli.actor.projects.read.DirectoryProjectReader
import ru.sber.cb.ap.gusli.actor.projects.read.DirectoryProjectReader.{ProjectReaded, ReadProject}

class ProjectToDistribTest extends ActorBaseTest("MakeDistrib") {
  private val projectPath = Paths.get("./src/test/scala/ru/sber/cb/ap/gusli/actor/core/diff/project/recursive/data/project")
  private val projectCopyPath = Paths.get("./src/test/scala/ru/sber/cb/ap/gusli/actor/core/diff/project/recursive/data/project-copy")
  private var currentProject: ActorRef = _
  private var prevProject: ActorRef = _
  
  "MakeDisribTest" when {
    val path = Paths.get("./target/mkdistribtest")
    val artName = "art-1"
    val projectDistrib = system.actorOf(ProjectToDistrib(path, artName, self))
    
    "preStart" should {
      "geg" in {
        import FolderNames._
        val artifactPath = path.resolve(artName)
        val apCtlPath = artifactPath.resolve(apCtl)
        val catPath = apCtlPath.resolve(categories)
        val catCreatePath =catPath.resolve(create)
        
        assert(artifactPath.toFile.exists)
        assert(catCreatePath.toFile.exists)
        
      }
    }
    "project 1 and 2" should {
      "be read" in {
        system.actorOf(DirectoryProjectReader(projectPath)) ! ReadProject()
  
        expectMsgPF() {
          case ProjectReaded(curr) =>
            currentProject = curr
            system.actorOf(DirectoryProjectReader(projectCopyPath)) ! ReadProject()
            expectMsgPF() {
              case ProjectReaded(prev) =>
                prevProject = prev
            }
        }
      }
    }
    
    "project differ sending Delta to DistribMaker" should {
      "receive DistribWasMade" in {
        system.actorOf(ProjectDiffer(currentProject, prevProject, projectDistrib))
        expectMsgPF() {
          case DistribWasMade(_) =>
        }
      }
    }
  }
}
