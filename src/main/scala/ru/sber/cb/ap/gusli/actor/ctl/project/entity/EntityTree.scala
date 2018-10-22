package ru.sber.cb.ap.gusli.actor.ctl.project.entity

import java.net.URL

import play.api.libs.json.Json
import ru.sber.cb.ap.gusli.actor.core.EntityMetaDefault
import ru.sber.cb.ap.gusli.actor.ctl.api.CtlToProjectConfig
import ru.sber.cb.ap.gusli.actor.ctl.model.Entity
import ru.sber.cb.ap.gusli.actor.ctl.model.CtlJsonProtocol._

import scala.collection.mutable
import scala.io.Source

object EntityTree {
  def apply(entitites: Set[Long], ctlConfig: CtlToProjectConfig): EntityTree = {
    val tree = new EntityTree(EntityLeaf(-1))
    entitites.foreach(e => addEntityWithParentsFromCtl(tree.root, e, ctlConfig.url))
    tree.root.meta = EntityMetaDefault(-1 ,"category", "not used", None)
    tree
  }
  
  private def addEntityWithParentsFromCtl(root: EntityLeaf, id: Long, url: URL): Unit = {
    def dlEntity(enId: Long) = {
      val entityLeaf = EntityLeaf(enId)
      entityLeaf.meta = downloadEntityMeta(enId, url)
      entityLeaf
    }
    
    val stack = mutable.Stack[EntityLeaf]()
    
    stack.push(dlEntity(id))
    
    while (!stack.head.meta.parentId.contains(0))
      stack.push(dlEntity(stack.head.meta.parentId.get))
    
    var prevEntity = root
    while (stack.nonEmpty)
      prevEntity = prevEntity.add(stack.pop())
  }
  

  private def downloadEntityMeta(id: Long, url: URL) = {
    val sE = Source.fromURL(url.toString + s"entity/$id").mkString
    val jsE = Json.parse(sE)
    val objE = Json.fromJson[Entity](jsE).get
    objE.toEntityMeta()
  }
}

class EntityTree(val root: EntityLeaf) {
  def workBreadth(func: EntityLeaf => Unit): Unit = {
    val queue = collection.mutable.Queue(root)
    workBreadthInner(queue)(func)
  }
  private def workBreadthInner(q: mutable.Queue[EntityLeaf])(func: EntityLeaf => Unit) {
    val el = q.dequeue()
    func(el)
    el.children.foreach(el => q.enqueue(el))
    if (q.nonEmpty)
      workBreadthInner(q)(func)
  }
}

case class EntityLeaf(id: Long) {
  import scala.collection.mutable._
  var meta: EntityMetaDefault = _
  val children = Set.empty[EntityLeaf]
  
  def add(id: Long): EntityLeaf = {
    if (children.contains(EntityLeaf(id)))
      children.find(_ == EntityLeaf(id)).get
    else {
      val a = EntityLeaf(id)
      children += a
      a
    }
  }
  
  def add(leaf: EntityLeaf): EntityLeaf = {
    if (children.contains(leaf))
      children.find(_ == EntityLeaf(leaf.id)).get
    else {
      children += leaf
      leaf
    }
  }
  
  def workDepth(func: EntityLeaf => Unit): Unit = {
    func(this)
    children.foreach(a=> a.workDepth(func))
  }
  
  override def toString: String = s"${id.toString} ${meta.name}"
}
