package ru.sber.cb.ap.gusli.actor.distrib

object FolderNames extends Enumeration {
  val apHdfs ="ap-hdfs"
  val apCtl = "ap-ctl"
  
  val categories = "categories"
  val workflows = "workflows"
  val entities = "entities"
  
  val create = "create"
  val change = "change"
  val delete = "delete"
}
