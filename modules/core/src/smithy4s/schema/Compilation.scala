package smithy4s.schema

import smithy4s.Lazy

/** Algebra for the staged implementation of interpreters, allowing for a generic handling of both caching and recursion
  * without requiring any mutation (lazy vals are a form of mutation).
  *
  * This is essentially a specialised applicative
  */
sealed trait Compilation[A] {
  def map[B](f: A => B): Compilation[B] = Compilation.Map(this, f)

  private[schema] def visit[C[_]](visitor: Compilation.Visitor[C]): C[A]
}

// scalafmt: {maxColumn = 120}
object Compilation {
  def pure[A](fa: A): Compilation[A] = Pure(fa)
  def cached[F[_], A](compiler: Compiler[F], schema: Schema[A]): Compilation[F[A]] = GetCached(compiler, schema)
  def recursive[F[_], A](compiler: Compiler[F], lazySchema: Lazy[Schema[A]])(
      make: (() => F[A]) => F[A]
  ): Compilation[F[A]] = Cyclic(compiler, lazySchema, make)
  def traverse[F[_], A, B](list: List[A])(f: A => Compilation[B]): Compilation[List[B]] = Traverse(list, f)

  def map2[F[_], A0, A1, Z](a0: Compilation[A0], a1: Compilation[A1])(f: (A0, A1) => Z): Compilation[Z] =
    traverse(List(a0, a1).asInstanceOf[List[Compilation[Any]]])(identity(_)).map { list =>
      f(list(0).asInstanceOf[A0], list(1).asInstanceOf[A1])
    }

  private final case class Pure[A](a: A) extends Compilation[A] {
    private[schema] def visit[C[_]](visitor: Compilation.Visitor[C]): C[A] = visitor.pure(a)
  }
  private final case class GetCached[F[_], A](compiler: Compiler[F], schema: Schema[A]) extends Compilation[F[A]] {
    private[schema] def visit[C[_]](visitor: Compilation.Visitor[C]): C[F[A]] = visitor.getCached(compiler, schema)
  }
  private final case class Cyclic[F[_], A](
      compiler: Compiler[F],
      lazySchema: Lazy[Schema[A]],
      make: (() => F[A]) => F[A]
  ) extends Compilation[F[A]] {
    private[schema] def visit[C[_]](visitor: Compilation.Visitor[C]): C[F[A]] =
      visitor.cyclic(compiler, lazySchema, make)
  }
  private final case class Traverse[F[_], A, B](list: List[A], f: A => Compilation[B]) extends Compilation[List[B]] {
    private[schema] def visit[C[_]](visitor: Compilation.Visitor[C]): C[List[B]] =
      visitor.traverse[A, B](list, a => f(a).visit(visitor))
  }
  private final case class Map[F[_], A, B](fa: Compilation[A], f: A => B) extends Compilation[B] {
    private[schema] def visit[C[_]](visitor: Compilation.Visitor[C]): C[B] =
      visitor.map(fa.visit(visitor), f)
  }

  trait Visitor[C[_]] {
    def getCached[F[_], A](compiler: Compiler[F], schema: Schema[A]): C[F[A]]
    def cyclic[F[_], A](compiler: Compiler[F], lazySchema: Lazy[Schema[A]], make: (() => F[A]) => F[A]): C[F[A]]
    def pure[A](a: A): C[A]
    def traverse[A, B](list: List[A], f: A => C[B]): C[List[B]]
    def map[A, B](list: C[A], f: A => B): C[B]
  }

}
