package com.ansvia.graph.util
package scalax
package rules

import scala.collection.mutable.HashMap

trait MemoisableRules extends Rules {
  def memo[In <: Memoisable, Out, A, X](key: AnyRef)
                                       (toRule: => In => Result[Out, A, X]) = {
    lazy val rule = toRule
    from[In] {in => in.memo(key, rule(in))}
  }

  override def ruleWithName[In, Out, A, X](name: String,
                                           f: In => rules.Result[Out, A, X]) = super.ruleWithName(
    name, (in: In) =>
      in match {
        case s: Memoisable => s.memo(name, f(in))
        case _ => f(in)
      }
  )
}

trait Memoisable {
  def memo[A](key: AnyRef, a: => A): A
}


object DefaultMemoisable {
  var debug = false
}

trait DefaultMemoisable extends Memoisable {
  protected val map = new HashMap[AnyRef, Any]

  def memo[A](key: AnyRef, a: => A) = {
    map.getOrElseUpdate(key, compute(key, a)).asInstanceOf[A]
  }

  protected def compute[A](key: AnyRef, a: => A): Any = a match {
    case success: Success[_, _] => onSuccess(key, success); success
    case other =>
      if (DefaultMemoisable.debug) println(key + " -> " + other)
      other
  }

  protected def onSuccess[S, T](key: AnyRef, result: Success[S, T]) {
    val Success(out, t) = result
    if (DefaultMemoisable.debug) println(key + " -> " + t + " (" + out + ")")
  }
}




