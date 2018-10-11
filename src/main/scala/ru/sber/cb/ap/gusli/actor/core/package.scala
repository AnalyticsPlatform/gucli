package ru.sber.cb.ap.gusli.actor

import akka.actor.ActorRef
import ru.sber.cb.ap.gusli.actor.core.Category.{CategoryMetaResponse, GetCategoryMeta, GetSubcategories, SubcategorySet}
import akka.pattern.ask
import akka.util.Timeout

import concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

package object core {
  implicit val timeout = Timeout(5 second)
  object EmptySet {
    def unapply[A](s: Set[A]): Boolean = s.isEmpty
  }

  object NonEmptySet {
    def unapply[A](s: Set[A]): Option[Set[A]] =
      if (s.isEmpty) None
      else Some(s)
  }

  def categoryPrinter(cat:ActorRef): Unit = {
    (cat ? GetCategoryMeta()).map{
      case CategoryMetaResponse(m)=>
        cprint("m="+m+"  c="+cat)
    }
    (cat ? GetSubcategories()).map{
      case SubcategorySet(s)=>
        for (c<-s) {
          categoryPrinter(c)
        }
    }
  }


  def cprint(m:Any): Unit = {
    println(Console.GREEN+m+Console.RESET)
  }
}
