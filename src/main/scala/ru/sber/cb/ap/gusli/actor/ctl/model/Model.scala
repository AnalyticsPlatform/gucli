package ru.sber.cb.ap.gusli.actor.ctl.model

import ru.sber.cb.ap.gusli.actor.core.dto.{EntityDto, WorkflowDto}
import ru.sber.cb.ap.gusli.actor.core.{EntityMetaDefault, WorkflowMetaDefault}

object Entity {
  def fromDto(eDto: EntityDto): Entity = Entity(eDto.name, Some(eDto.path), eDto.id, parentId = eDto.parentId.getOrElse(0))
}

case class Entity(
  name: String,
  path: Option[String],
  id: Long,
  storage: String = "HDFS",
  parentId: Long) {
  override def toString: String = s"$id $name"
  
  def toEntityMeta(): EntityMetaDefault = EntityMetaDefault(id, name, path.getOrElse(""), Some(parentId))
}

/**XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX*/

case class Category(
                    id: Int,
                    name: String,
                    deleted: Boolean,
                    parentId: Int = 0,
                    cat_id: Option[Int] = None,
                    action: Option[String] = None)

object Category {
  def namedCategory(name: String): Category = new Category(-1, name, false, 0, Some(-1), None)
}

/**XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX*/

object Param {
  def fromDto(wDto: WorkflowDto, catName: String): Seq[Param] = {
    val newMap = wDto.params ++ dtoToMap(wDto, catName)
    val params = for ((k,v) <- newMap) yield Param(Some(-1), k, Some(v))
    params.toSeq
  }
  
  private def dtoToMap(wDto: WorkflowDto, catName: String) = {
    val m = collection.mutable.Map.empty[String, String]
    if (wDto.sqlMap.nonEmpty)
      m += "sql.map" -> ("hdfs:///user/${user.name}/" + s"$catName/${wDto.name}/sql.map")
    if (wDto.init.nonEmpty)
      m += "sql.init" -> ("hdfs:///user/${user.name}/" + s"$catName/${wDto.name}/sql.init")
    if (wDto.sql.nonEmpty)
      m += "sql.file" -> ("hdfs:///user/${user.name}/" + s"$catName/${wDto.name}/${wDto.name}.sql")
    if (wDto.queue.nonEmpty)
      m += "mapreduce.job.queuename" -> wDto.queue.get
    if (wDto.grenkiVersion.nonEmpty)
      m += "grenki" -> wDto.grenkiVersion.get
    if (wDto.user.nonEmpty)
      m += "user.name" -> wDto.user.get
    m.toMap
  }
  
}

case class Param(
  wf_id: Option[Int],
  param: String,
  prior_value: Option[String])

/**XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX*/

object Wf {
  def fromDto(categoryName: String, wDto: WorkflowDto): Wf = Wf(
    name = wDto.name,
    param = Param.fromDto(wDto, categoryName),
    scheduled = false,
    id = -1,
    category = categoryName,
    `type` = "principal",
    engine = "oozie",
    deleted = false
  )
}

case class Wf(
  name: String,
  param: Seq[Param],
  scheduled: Boolean,
  id: Int,
  category: String,
  `type`: String,
  engine: String,
  deleted: Boolean,
  eventAwaitStrategy: Option[String] = Some("or"))

/**XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX*/

case class WfExt(
  wf: Wf,
  connectedEntities: Seq[Int],
  connectedStats: Seq[Int]) {
  
  import ru.sber.cb.ap.gusli.actor.core.WorkflowMeta
  
  def convertToWorkflowMeta(): WorkflowMeta = {
    val Wf(name, param, scheduled, id, category, t, engine, deleted, eventAwaitStrategy) = wf
    ???
    //TODO: Implement
    WorkflowMetaDefault(name, Map.empty)
  }
}

/**XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX*/

case class StatValPost(
                        loading_id: Int,
                        entity_id: Int,
                        stat_id: Int,
                        avalue: Array[String])

/**XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX*/

case class CategoryDto(name: String, workflows: Set[WorkflowDto])