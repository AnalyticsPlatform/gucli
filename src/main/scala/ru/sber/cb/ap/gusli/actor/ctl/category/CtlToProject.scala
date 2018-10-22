package ru.sber.cb.ap.gusli.actor.ctl.category

import ru.sber.cb.ap.gusli.actor.BaseActor
import ru.sber.cb.ap.gusli.actor.core.{Project, ProjectMetaDefault}

trait CtlToProject extends BaseActor {
  
  protected def createProject() = context.system.actorOf(Project(ProjectMetaDefault("project")))
}
