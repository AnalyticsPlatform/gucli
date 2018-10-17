package ru.sber.cb.ap.gusli.actor.core.dto

import ru.sber.cb.ap.gusli.actor.core.EntityMeta

object EntityDto {
  def apply(meta: EntityMeta, children: Set[EntityDto]): EntityDto =
    new EntityDto(meta.id, meta.name, meta.path, meta.parentId, children)
}

case class EntityDto(id: Long,
                     name: String,
                     path: String,
                     parentId: Option[Long],
                     children: Set[EntityDto]
                    ) extends EntityMeta

