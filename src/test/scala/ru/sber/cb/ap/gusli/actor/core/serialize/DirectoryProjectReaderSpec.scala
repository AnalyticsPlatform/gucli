package ru.sber.cb.ap.gusli.actor.core.serialize

import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import ru.sber.cb.ap.gusli.actor.core.Category.{apply => _, _}
import ru.sber.cb.ap.gusli.actor.core.Entity.{EntityMetaResponse, GetEntityMeta}
import ru.sber.cb.ap.gusli.actor.core.Project.{apply => _, _}
import ru.sber.cb.ap.gusli.actor.projects.DirectoryProjectReader
import ru.sber.cb.ap.gusli.actor.projects.DirectoryProjectReader._

class DirectoryProjectReaderSpec extends TestKit(ActorSystem("DirectoryProjectSpec")) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {
  val directoryProjectReader: ActorRef = system.actorOf(DirectoryProjectReader())
  val correctPath = Paths.get(".\\src\\test\\resources\\project")
  val incorrectPath = Paths.get("incorrect_path_here")
  
  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }
  
  "Directory project reader" when {
    "receive ReadProject(correctPath)" should {
      var project: ActorRef = null
      var cbCategory: ActorRef = null
      var apCategory: ActorRef = null
      var rbCategory: ActorRef = null
      "send back ProjectReaded(project)" in {
        directoryProjectReader ! ReadProject(correctPath)
        expectMsgPF() {
          case ProjectReaded(inputProject) => project = inputProject
        }
      }
      "and project receiving GetProjectMeta should send back ProjectMetaResponse" in {
        project ! GetProjectMeta()
        expectMsg(ProjectMetaResponse("project_test"))
      }
      "receiving GetEntityRoot should send back EntityRoot" in {
        project ! GetEntityRoot()
        expectMsgPF() {
          case EntityRoot(root) =>
            root ! GetEntityMeta()
            expectMsg(EntityMetaResponse(105000000, "entity-root", "/data"))
        }
      }
      "receiving GetCategoryRoot should send back CategoryRoot" in {
        project ! GetCategoryRoot()
        expectMsgPF() {
          case CategoryRoot(root) =>
            cbCategory = root
            root ! GetCategoryMeta()
            expectMsg(CategoryMetaResponse("cb"))
        }
      }
      "receiving FindEntity(1) should send back EntityNotFound" in {
        project ! FindEntity(1)
        expectMsgAnyClassOf(classOf[EntityNotFound])
      }
      "receiving FindEntity(105000000) should send back EntityFound" in {
        project ! FindEntity(105000000)
        expectMsgAnyClassOf(classOf[EntityFound])
      }
      "receiving FindEntity(105060000) should send back EntityFound" in {
        project ! FindEntity(105060000)
        expectMsgAnyClassOf(classOf[EntityFound])
      }
      "receiving FindEntity(105067000) should send back EntityFound" in {
        project ! FindEntity(105067000)
        expectMsgAnyClassOf(classOf[EntityFound])
      }
      "receiving FindEntity(105067100) should send back EntityFound" in {
        project ! FindEntity(105067100)
        expectMsgAnyClassOf(classOf[EntityFound])
      }
      "receiving FindEntity(105067200) should send back EntityFound" in {
        project ! FindEntity(105067200)
        expectMsgAnyClassOf(classOf[EntityFound])
      }
      "receiving FindEntity(105067300) should send back EntityFound" in {
        project ! FindEntity(105067300)
        expectMsgAnyClassOf(classOf[EntityFound])
      }
      "and category receiving ListWorkflow should send back WorkflowList with size 5" in {
        cbCategory ! ListWorkflow()
        expectMsgPF() {
          case WorkflowList(list) => assert(list.size == 5)
        }
      }
      "receiving ListSubcategory send back SubcategoryList with size 1 (ap)" in {
        cbCategory ! ListSubcategory()
        expectMsgPF() {
          case SubcategoryList(list) =>
            assert(list.size == 1)
            apCategory = list(0)
        }
      }
      "apCategory should include rbCategory" in {
        apCategory ! ListSubcategory()
        expectMsgPF() {
          case SubcategoryList(list) =>
            assert(list.size == 1)
            rbCategory = list(0)
        }
      }
      "rbCategory receiving ListWorkflow send back WorkflowList with size 3" in {
        rbCategory ! ListWorkflow()
        expectMsgPF() {
          case WorkflowList(list) => assert(list.size == 3)
        }
      }
    }
  }

}