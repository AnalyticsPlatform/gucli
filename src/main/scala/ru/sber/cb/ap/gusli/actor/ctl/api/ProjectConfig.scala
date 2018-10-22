package ru.sber.cb.ap.gusli.actor.ctl.api

import java.net.URL
import com.typesafe.config.ConfigFactory

case object EnvTypes extends Enumeration {
  type Env = Value
  val Dev = Value
  val St = Value
}

//query="cb/ap*"
trait CtlToProjectConfig {
  private val appConfig = ConfigFactory.load()
  private val devUrl = new URL(appConfig.getString("ctl.url.dev"))
  private val devSt = new URL(appConfig.getString("ctl.url.st"))
  
  def env: EnvTypes.Env
  
  def url: URL = {
    env match {
      case EnvTypes.Dev => devUrl
      case EnvTypes.St => devSt
    }
  }
}

case class CtlToProjectConfigWithCategoryNames(override val env: EnvTypes.Env, categories: Set[String] = Set.empty, workflows: Set[String] = Set.empty) extends CtlToProjectConfig

case class CtlToProjectConfigFilter(override val env: EnvTypes.Env, categoryFilter: Option[String] = None, workflowFilter: Option[String] = None) extends CtlToProjectConfig {
  def toCtlToProjectConfigWithCategoryNames() = {}//ToDo
}
