package ru.sber.cb.ap.gusli.actor.distrib

import java.nio.file.Paths

import ru.sber.cb.ap.gusli.actor.core.ActorBaseTest
import ru.sber.cb.ap.gusli.actor.core.dto.{CategoryDto, WorkflowDto}
import ru.sber.cb.ap.gusli.actor.distrib.CategoryToDisrtib.CategoryWritten
import concurrent.duration._
class CategoryToDisrtibTest extends ActorBaseTest("CategoryToDisrtib") {
  val path = Paths.get("./target/mkdistribtest/cat-create-test/")
  val cDto = createCategoryTree()
  
  "CategoryToDistrib" when {
    "preStart" should {
      "create json entities in distrib" in {
        val root = CategoryDto("root")
        system.actorOf(CategoryToDisrtib(path, cDto.name, cDto, root, self))
        expectMsg(3 hours, CategoryWritten(cDto))
        import FolderNames._
        assert(path.resolve(apCtl).resolve(categories).resolve(create).toFile.listFiles.size == 6)
        assert(path.resolve(apCtl).resolve(workflows).resolve(create).toFile.listFiles.size == 7)
      }
    }
  }
  
  
  private def createCategoryTree(): CategoryDto = {
    val sqlForWf = Map("1.sql" -> "select 1 from table", "2.sql" -> "select 1 from table")
    val mapForWf = Map("1.map" -> "a:b", "2.map" -> "a:b")
    val initForWf = Map("init1.sql" -> "select 1 from table", "init2.sql" -> "select 1 from table")
    
    val wf11 = WorkflowDto("1", sqlForWf, mapForWf, initForWf, entities = Set(1, 2, 3), user = Some("Iluha"))
    val wf12 = WorkflowDto("2", sqlForWf, mapForWf, initForWf, entities = Set(1, 2, 3))
    val wf12211 = WorkflowDto("3", sqlForWf, mapForWf, initForWf, entities = Set(1, 2, 3), stats = Set(1, 2, 3))
    
    val c1221 = CategoryDto("1221", workflows = Set(wf12211))
    val c122 = CategoryDto("122", subcategories = Set(c1221))
    val c121 = CategoryDto("121")
    val c11 = CategoryDto("11")
    val c12 = CategoryDto("12", subcategories = Set(c121, c122))
    val c1 = CategoryDto("1", subcategories = Set(c11, c12), workflows = Set(wf11, wf12))
    c1
  }
}
