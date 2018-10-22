package ru.sber.cb.ap.gusli.actor.distrib

import java.nio.file.Paths

import ru.sber.cb.ap.gusli.actor.core.ActorBaseTest
import ru.sber.cb.ap.gusli.actor.core.dto.EntityDto
import ru.sber.cb.ap.gusli.actor.distrib.EntityToDistrib.EntityWritten
import concurrent.duration._
class EntityToDistribTest extends ActorBaseTest("EntityToDistrib") {
  
  val path = Paths.get("./target/mkdistribtest/entity-create-test/")
  
  val eDto = createEntityTree()
  
  "EntityToDistrib" when {
    "preStart" should {
      "create json entities in distrib" in {
        system.actorOf(EntityToDistrib(path, eDto, self))
        expectMsg(EntityWritten(eDto))
        println(eDto.children)
        assert(path.toFile.listFiles.size == 6)
      }
    }
  }
  
  private def createEntityTree(): EntityDto = {
    val eDto1221 = EntityDto(1221, "1221", "", Some(122), Set.empty)
    val eDto122 = EntityDto(122, "122", "", Some(12), Set(eDto1221))
    val eDto121 = EntityDto(121, "121", "", Some(12), Set.empty)
    val eDto11 = EntityDto(11, "11", "", Some(1), Set.empty)
    val eDto12 = EntityDto(12, "12", "", Some(1), Set(eDto121, eDto122))
    val eDto1 = EntityDto(1, "1", "", Some(0), Set(eDto11, eDto12))
    eDto1
  }
}
