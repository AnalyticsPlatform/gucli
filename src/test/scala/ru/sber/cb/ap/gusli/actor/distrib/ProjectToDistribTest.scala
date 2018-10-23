package ru.sber.cb.ap.gusli.actor.distrib

import java.nio.file.Paths

import ru.sber.cb.ap.gusli.actor.core.ActorBaseTest

class ProjectToDistribTest extends ActorBaseTest("MakeDistrib") {
  
  "MakeDisribTest" when {
    val path = Paths.get("./target/mkdistribtest")
    val artName = "art-1"
    system.actorOf(ProjectToDistrib(path, artName, self))
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
  }
}
