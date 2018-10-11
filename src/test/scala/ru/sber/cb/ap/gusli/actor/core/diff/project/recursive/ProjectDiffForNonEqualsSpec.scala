package ru.sber.cb.ap.gusli.actor.core.diff.project.recursive

import java.nio.file.Paths

import akka.actor.ActorRef
import akka.testkit.TestProbe
import org.scalatest.Ignore
import ru.sber.cb.ap.gusli.actor.core.ActorBaseTest
import ru.sber.cb.ap.gusli.actor.core.Category.{GetSubcategories, SubcategorySet}
import ru.sber.cb.ap.gusli.actor.core.Project.{CategoryRoot, GetCategoryRoot}
import ru.sber.cb.ap.gusli.actor.core.diff.ProjectDiffer
import ru.sber.cb.ap.gusli.actor.core.diff.ProjectDiffer.ProjectDelta
import ru.sber.cb.ap.gusli.actor.projects.read.DirectoryProjectReader
import ru.sber.cb.ap.gusli.actor.projects.read.DirectoryProjectReader.{ProjectReaded, ReadProject}
import ru.sber.cb.ap.gusli.actor.projects.write.ProjectWriter
import ru.sber.cb.ap.gusli.actor.projects.write.ProjectWriter.{ProjectWrited, WriteProject}

import concurrent.duration._

@Ignore
class ProjectDiffForNonEqualsSpec extends ActorBaseTest("ProjectDiffForNonEqualsSpec") {
  private val projectPath = Paths.get("./src/test/scala/ru/sber/cb/ap/gusli/actor/core/diff/project/recursive/data/project")
  private val projectCopyPath = Paths.get("./src/test/scala/ru/sber/cb/ap/gusli/actor/core/diff/project/recursive/data/project-copy")
  private var currentProject: ActorRef = _
  private var prevProject: ActorRef = _
  private val receiver = TestProbe()

  "Directory project differ" when {
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

    "create ProjectDiffer" in {
      system.actorOf(ProjectDiffer(currentProject, prevProject, receiver.ref))
      receiver.expectMsgPF() {
        case ProjectDelta(p) =>
          p ! GetCategoryRoot()
          expectMsgPF(){
            case CategoryRoot(root)=>
              root ! GetSubcategories()
              expectMsgPF(){
                case SubcategorySet(set)=>
                  println(set)
              }
          }
          system.actorOf(ProjectWriter(p, Paths.get("./target/ProjectDiffForNonEqualsSpec"))) ! WriteProject()
          expectMsg(ProjectWrited())
      }
    }
  }
}