package ru.sber.cb.ap.gusli.actor.core.diff

import ru.sber.cb.ap.gusli.actor.core.CategoryMeta
import ru.sber.cb.ap.gusli.actor.core.dto.{CategoryDto, WorkflowDto}

object CategoryDtoDiffer {

  def getDiff(current: CategoryDto, previous: CategoryDto): CategoryDtoDifferResponse = {
    if (current == previous)
      return CategoryDtoEquals(current, previous)

    throwExceptionIfNamesDiffer(current, previous)

    val diffSubs = getSubcategoryDiff(current, previous)
    val diffWfs = getWorkflowsDiff(current, previous)

    CategoryDtoDelta(current.copy(subcategories = diffSubs, workflows = diffWfs))
  }

  def throwExceptionIfNamesDiffer(current: CategoryDto, previous: CategoryDto): Unit = {
    val currMeta = current.asInstanceOf[CategoryMeta]
    val prevMeta = previous.asInstanceOf[CategoryMeta]

    if (currMeta.name != prevMeta.name)
      throw new RuntimeException("A category must have the same name for comparing")
  }

  protected def getSubcategoryDiff(current: CategoryDto, previous: CategoryDto): Set[CategoryDto] = {
    val currSubs = getSubcategoryMap(current)
    val prevSubs = getSubcategoryMap(previous)

    var diffSubs: Set[CategoryDto] = Set.empty

    val equalSubNames = currSubs.keySet intersect prevSubs.keySet
    for (n <- equalSubNames) {
      val c = currSubs(n)
      val p = prevSubs(n)

      getDiff(c, p) match {
        case CategoryDtoEquals(c1, c2) =>
          //todo: replace by the log message
          println(s"CategoryDtoEquals:$c1, $c2")
        case CategoryDtoDelta(d) =>
          diffSubs += d
      }
    }

    val diffSubNames = currSubs.keySet diff prevSubs.keySet
    for (n <- diffSubNames)
      diffSubs += currSubs(n)

    diffSubs
  }

  protected def getSubcategoryMap(categoryDto: CategoryDto): Map[String, CategoryDto] =
    categoryDto.subcategories.map(x => x.name -> x).toMap

  protected def getWorkflowsDiff(current: CategoryDto, previous: CategoryDto): Set[WorkflowDto] = {
    val currWfs = getWorkflowMap(current)
    val prevWfs = getWorkflowMap(previous)

    var diffWfs: Set[WorkflowDto] = Set.empty

    val equalWfNames = currWfs.keySet intersect prevWfs.keySet
    for (n <- equalWfNames) {
      val c = currWfs(n)
      val p = prevWfs(n)

      if (!isWorkflowEquals(c, p))
        diffWfs += c
    }

    val diffWfNames = currWfs.keySet diff prevWfs.keySet
    for (n <- diffWfNames)
      diffWfs += currWfs(n)

    diffWfs
  }

  protected def getWorkflowMap(categoryDto: CategoryDto): Map[String, WorkflowDto] =
    categoryDto.workflows.map(x => x.name -> x).toMap

  protected def isWorkflowEquals(current: WorkflowDto, previous: WorkflowDto): Boolean =
    current == previous

  abstract class CategoryDtoDifferResponse

  case class CategoryDtoEquals(currentCat: CategoryDto, prevCat: CategoryDto) extends CategoryDtoDifferResponse

  case class CategoryDtoDelta(deltaCat: CategoryDto) extends CategoryDtoDifferResponse

}