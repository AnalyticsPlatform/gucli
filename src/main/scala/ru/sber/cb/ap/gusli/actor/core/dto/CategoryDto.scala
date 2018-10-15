package ru.sber.cb.ap.gusli.actor.core.dto

import ru.sber.cb.ap.gusli.actor.core.CategoryMeta

case class CategoryDto(name: String,
                       sqlMap: Map[String, String] = Map.empty,
                       init: Map[String, String] = Map.empty,
                       user: Option[String] = None,
                       queue: Option[String] = None,
                       grenkiVersion: Option[String] = None,
                       params: Map[String, String] = Map.empty,
                       stats: Set[Long] = Set.empty,
                       entities: Set[Long] = Set.empty,
                       subcategories: Set[CategoryDto] = Set.empty,
                       workflows: Set[WorkflowDto] = Set.empty) extends CategoryMeta

object CategoryDto {
  def apply(m: CategoryMeta): CategoryDto =
    CategoryDto(m, Set.empty[CategoryDto], Set.empty[WorkflowDto])

  def apply(m: CategoryMeta, subcategories: Set[CategoryDto]): CategoryDto =
    CategoryDto(m, subcategories, Set.empty[WorkflowDto])

  def apply(m: CategoryMeta, subcategories: Set[CategoryDto], workflows: Set[WorkflowDto]): CategoryDto =
    CategoryDto(m.name, m.sqlMap, m.init, m.user, m.queue, m.grenkiVersion, m.params, m.stats, m.entities, subcategories, workflows)
}