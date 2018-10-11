package ru.sber.cb.ap.gusli.actor.core.diff

import akka.actor.{ActorRef, Props}
import ru.sber.cb.ap.gusli.actor.core.Category._
import ru.sber.cb.ap.gusli.actor.core.clone.CategoryCloner.CategoryCloneSuccessfully
import ru.sber.cb.ap.gusli.actor.core.clone.{CategoryCloner, CategorySetCloner}
import ru.sber.cb.ap.gusli.actor.core.clone.CategorySetCloner.CategorySetCloneSuccessfully
import ru.sber.cb.ap.gusli.actor.core.diff.CategoryDiffer.{CategoryDelta, CategoryEquals}
import ru.sber.cb.ap.gusli.actor.core.diff.CategoryMetaDiffer.{CategoryMetaDelta, CategoryMetaEquals}
import ru.sber.cb.ap.gusli.actor.core.extractor.SubcategoryExtractor
import ru.sber.cb.ap.gusli.actor.core.extractor.SubcategoryExtractor.SubcategoryExtracted
import ru.sber.cb.ap.gusli.actor.core.{Category, CategoryMeta}
import ru.sber.cb.ap.gusli.actor.{BaseActor, Response}
  import akka.pattern.ask
  import akka.util.Timeout

  import concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global

object CategoryDiffer {
  def apply(currentCat: ActorRef, prevCat: ActorRef, receiver: ActorRef): Props = Props(new CategoryDiffer(currentCat, prevCat, receiver))

  abstract class CategoryDifferResponse extends Response

  case class CategoryEquals(currentCat: ActorRef, prevCat: ActorRef) extends CategoryDifferResponse

  case class CategoryDelta(deltaCat: ActorRef) extends CategoryDifferResponse

}

class CategoryDiffer(currentCat: ActorRef, prevCat: ActorRef, receiver: ActorRef) extends BaseActor {
  implicit val timeout = Timeout(5 hour)

  private var currProject: Option[ActorRef] = None
  private var deltaCatMeta: Option[CategoryMeta] = None
  private var categoryDelta: Option[ActorRef] = None
  private var currMap: Option[Map[String, ActorRef]] = None
  private var prevMap: Option[Map[String, ActorRef]] = None
  private var newSubcategoryCopied = false
  private var equalsSubcategoryCopied = false
  private var subcategoriesToCompareCount = 0
  private var isCategoryMetaEquals = false
  private var isNewSubcategoryEmpty = false
  private var isEqualsSubcategoryEmpty = false

  override def preStart(): Unit = {
    currentCat ! GetProject()
    context.actorOf(CategoryMetaDiffer(currentCat, prevCat, self))
    context.actorOf(SubcategoryExtractor(currentCat, self))
    context.actorOf(SubcategoryExtractor(prevCat, self))
  }

  override def receive: Receive = {
    case ProjectResponse(p) =>
      currProject = Some(p)
      createCategoryDelta()
    case CategoryMetaDelta(d) =>
      deltaCatMeta = Some(d)
      createCategoryDelta()
    case CategoryMetaEquals(_, _, m) =>
      deltaCatMeta = Some(m)
      isCategoryMetaEquals = true
      createCategoryDelta()
    case SubcategoryExtracted(cat, map) =>
      if (cat == currentCat)
        currMap = Some(map)
      else if (cat == prevCat)
        prevMap = Some(map)
      else throw new RuntimeException(s"Unexpectable `SubcategoryExtracted` sender $sender")
      compareSubcategories()

    case CategorySetCloneSuccessfully() =>
      newSubcategoryCopied = true
      checkFinish()
    case CategoryEquals(_, _) =>
      subcategoriesToCompareCount -= 1
      equalsSubcategoryCopied = subcategoriesToCompareCount == 0
      checkFinish()
    case CategoryDelta(d)=>
      context.actorOf(CategoryCloner(currentCat, d, self))
    case CategoryCloneSuccessfully()=>
      subcategoriesToCompareCount -= 1
      equalsSubcategoryCopied = subcategoriesToCompareCount == 0
      checkFinish()
  }

  def createCategoryDelta(): Unit = {
    for (p <- currProject; m <- deltaCatMeta)
      categoryDelta = Some(context.system.actorOf(Category(m, p)))
  }

  def compareSubcategories(): Unit = {
    for (d <- categoryDelta; c <- currMap; p <- prevMap) {
      copyNewSubcategories(d, c, p)
      compareEqualsSubcategories(d, c, p)
      checkFinish()
    }
  }

  def copyNewSubcategories(copyTo: ActorRef, curr: Map[String, ActorRef], prev: Map[String, ActorRef]): Unit = {
    val news = curr.keySet diff prev.keySet
    if (news.isEmpty) {
      newSubcategoryCopied = true
      isNewSubcategoryEmpty = true
    }
    else if (news.nonEmpty) {
      val newCats = for (n <- news) yield curr(n)

      context.actorOf(CategorySetCloner(copyTo, newCats, self))
    }
  }

  def compareEqualsSubcategories(copyTo: ActorRef, curr: Map[String, ActorRef], prev: Map[String, ActorRef]): Unit = {
    val eq = curr.keySet intersect prev.keySet
    if (eq.isEmpty) {
      equalsSubcategoryCopied = true
      isEqualsSubcategoryEmpty = true
    }
    else if (eq.nonEmpty){
      subcategoriesToCompareCount = eq.size
      for (n <- eq) {
        val c = curr(n)
        val d = prev(n)
        context.actorOf(CategoryDiffer(c, d, self))
      }
    }
  }

  def checkFinish(): Unit = {
    if (newSubcategoryCopied && equalsSubcategoryCopied) {
      if (isCategoryMetaEquals && isNewSubcategoryEmpty && isEqualsSubcategoryEmpty) {
        receiver ! CategoryEquals(currentCat, prevCat)
        context.stop(self)
      }
      else {
        receiver ! CategoryDelta(categoryDelta.get)
        context.stop(self)
      }
    }
  }
}
