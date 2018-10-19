package ru.sber.cb.ap.gusli.actor.core.dto

import ru.sber.cb.ap.gusli.actor.core.ProjectMeta

case class ProjectDto(name:String, entityRoot:EntityDto, categoryRoot:CategoryDto) extends ProjectMeta
