package ru.sber.cb.ap.gusli.actor

package object core {
  object EmptySet {
    def unapply[A](s: Set[A]): Boolean = s.isEmpty
  }

  object NonEmptySet {
    def unapply[A](s: Set[A]): Option[Set[A]] =
      if (s.isEmpty) None
      else Some(s)
  }

  def cprint(m:Any): Unit = {
    println(Console.GREEN+m+Console.RESET)
  }
}
