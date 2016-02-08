/**
  * Created by Nicolas A Perez (@anicolaspp) on 2/8/16.
  */

package dogs

import dogs.Order._

import scala.annotation.tailrec

import scala.Unit


/**
  * Represent a range [x, y] that can be generated by using discrete operations
  */
sealed class ARange[A] (val start: A, val end: A) {


  /**
    * Generates the elements of the range [start, end] base of the discrete operations
    */
  def generate()(implicit discrete: Enum[A]): List[A] = gen (start, end, El())(_=>{})

  /**
    * Generates the elements of the range [end, start] base of the discrete operations
    */
  def reverse()(implicit discrete: Enum[A]): List[A] = {
    gen(end, start, El())(_=>{})(new Enum[A] {
      override def pred(x: A): A = discrete.succ(x)
      override def succ(x: A): A = discrete.pred(x)
      override def apply(l: A, r: A): Ordering = discrete.apply(l, r)
    })
  }

  /**
    * Verify is x is in range [start, end]
    */
  def contains(x: A)(implicit discrete: Enum[A]) =
    discrete.ge(x, start) && discrete.le(x, end)


  /**
    * Apply function f to each element in range [star, end]
    */
  def foreach(f: A => Unit)(implicit discrete: Enum[A]): Unit = {
    val ignore = gen(start, end, El())(f)
  }

  def map[B](f: A => B)(implicit discrete: Enum[A]): List[B] =
    genMap[B](start, end, El())(f)

  def foldLeft[B](s: B, f: (B, A) => B)(implicit discrete: Enum[A]): B =
    generate().foldLeft(s)(f)

  @tailrec private def genMap[B](a: A, b: A, xs: List[B])(f: A => B)(implicit discrete: Enum[A]): List[B] = {
    if (discrete.compare(a, b) == EQ) {
      xs ::: Nel(f(a), El())
    } else if (discrete.adj(a, b)) {

      xs ::: Nel(f(a), Nel(f(b), El()))
    } else {
      genMap(discrete.succ(a), b, xs ::: (Nel(f(a), El())))(f)
    }
  }

  @tailrec private def gen(a: A, b: A, xs: List[A])(f: A=>Unit)(implicit discrete: Enum[A]): List[A] = {
      if (discrete.compare(a, b) == EQ) {
        f(a)
        xs ::: Nel(a, El())
      } else if (discrete.adj(a, b)) {
        f(a)
        f(b)
        xs ::: Nel(a, Nel(b, El()))
      }
      else {
        f(a)
        gen(discrete.succ(a), b, xs ::: (Nel(a, El())))(f)
      }
    }

  def apply(start: A, end: A): ARange[A] = ARange.apply(start, end)
}

object ARange {
  def apply[A](x: A, y: A) = new ARange[A](x, y)
}
