package ru.sber.cb.ap.gusli.actor.ctl.project

import akka.actor.ActorRef
import org.scalatest.Ignore
import ru.sber.cb.ap.gusli.actor.core.Category.{CategoryMetaResponse, GetCategoryMeta, GetSubcategories, SubcategorySet}
import ru.sber.cb.ap.gusli.actor.core.{ActorBaseTest, CategoryMetaDefault, EntityMetaDefault}
import ru.sber.cb.ap.gusli.actor.core.Entity.{ChildrenEntityList, EntityMetaResponse, GetChildren, GetEntityMeta}
import ru.sber.cb.ap.gusli.actor.core.Project._
import ru.sber.cb.ap.gusli.actor.core.dto.WorkflowDto
import ru.sber.cb.ap.gusli.actor.ctl.api.{CtlToProjectConfigWithCategoryNames, EnvTypes}
import ru.sber.cb.ap.gusli.actor.ctl.category.CategoryDtoDownloaderByName
import ru.sber.cb.ap.gusli.actor.ctl.category.CategoryDtoDownloaderByName.CategoryDtoResponse
import ru.sber.cb.ap.gusli.actor.ctl.project.ProjectCreator.ProjectCreated

import concurrent.duration._

@Ignore
class ProjectCreatorTest extends ActorBaseTest("ProjectCreatorTest") {
  val ctlConfig = CtlToProjectConfigWithCategoryNames(EnvTypes.Dev, Set("cb/ap/rb", "cb/ap/rb/dev"))
  var mapWithCategoryDto: Map[String, Set[WorkflowDto]] = _
  var project: ActorRef = _
  
  "ProjectCreatorTest" when {
    "downloading MapDto" should {
      "download them" in {
        system.actorOf(CategoryDtoDownloaderByName(ctlConfig, self))
        expectMsgPF() { case CategoryDtoResponse(m) => {
          println(m)
          mapWithCategoryDto = m
        }
        }
      }
      
      "send back ProjectCreated" in {
        system.actorOf(ProjectCreator(ctlConfig, mapWithCategoryDto, self))
        expectMsgPF() { case ProjectCreated(p) => project = p
        }
      }
      
      "send back entity with children" in {
        project ! GetEntityRoot()
        expectMsgPF() { case EntityRoot(eRoot) => eRoot ! GetChildren()
          expectMsgPF() {
            case ChildrenEntityList(list) => assert(list.size == 1)
            list.head ! GetEntityMeta()
            expectMsgPF() {
              case EntityMetaResponse(EntityMetaDefault(900000000, _, _, _)) =>
            }
          }
        }
      }
      
      "send back category with children" in {
        project ! GetCategoryRoot()
        expectMsgPF() { case CategoryRoot(cRoot) => cRoot ! GetSubcategories()
          expectMsgPF() { case SubcategorySet(set) => assert(set.size == 1)
            set.head ! GetCategoryMeta()
            expectMsgPF() { case CategoryMetaResponse(CategoryMetaDefault("cb", _, _, _, _, _, _, _, _)) =>
            }
          }
        }
      }
    }
  }
  
}
