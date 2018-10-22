package ru.sber.cb.ap.gusli.actor.ctl.api

import org.scalatest.{FlatSpec, Ignore}

@Ignore
class ProjectConfigSpec extends FlatSpec {
  
  "CtlToProjectConfigWithCategoryNames with DEV env" should "return dev url" in {
    val ctlConfig = CtlToProjectConfigWithCategoryNames(EnvTypes.Dev, Set.empty)
    assert(ctlConfig.url.toString == "hide")
  }
  
  "CtlToProjectConfigWithCategoryNames with another DEV declaration" should "return dev url" in {
    val env = EnvTypes.withName("Dev")
    val ctlConfig = CtlToProjectConfigWithCategoryNames(env, Set.empty)
    assert(ctlConfig.url.toString == "hide")
  }
}
