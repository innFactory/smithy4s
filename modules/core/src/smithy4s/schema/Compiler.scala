package smithy4s.schema

import smithy4s.Lazy
import smithy4s.kinds.OptionK

/** This is what the integrations provider have to implement. Unlike smithy4s 0.18, the algebra ensures that
  * cross-cutting concern (like caching and recursion) are not leaking into what the users is required to provide.
  */
trait Compiler[F[_]] {
  def apply[A](fa: Schema[A]): Compilation[F[A]]

  final def compileFull[A](schema: Schema[A]): F[A] = {
    val (callMap, runtime) =
      apply(schema).visit(Compiler.Visitor).run(Compiler.CallMap.empty)
    runtime.run(callMap.prune)
  }
}

object Compiler {

  type Optional[F[_]] = Compiler[OptionK[F, *]]

  def getOrElse[F[_]](
      possible: Compiler.Optional[F],
      default: Compiler[F]
  ): Compiler[F] = new Compiler[F] {
    def apply[A](schema: Schema[A]): Compilation[F[A]] =
      Compilation.map2(possible(schema), default(schema))(_.getOrElse(_))
  }

  def covariantStatic[F[+_]](alwaysFail: F[Nothing]): Compiler[F] =
    new Compiler[F] {
      def apply[A](fa: Schema[A]): Compilation[F[A]] =
        Compilation.pure(alwaysFail)
    }

  def contravariantStatic[F[-_]](empty: F[Any]): Compiler[F] =
    new Compiler[F] {
      def apply[A](fa: Schema[A]): Compilation[F[A]] =
        Compilation.pure(empty)
    }

  final case class CompileTime[A](run: CallMap => (CallMap, A)) {
    final def map[B](f: A => B): CompileTime[B] = CompileTime { state =>
      val (newState, a) = run(state)
      (newState, f(a))
    }

    final def flatMap[B](f: A => CompileTime[B]): CompileTime[B] =
      CompileTime { state =>
        val (newState1, a) = run(state)
        f(a).run(newState1)
      }

    final def flatTap[B](f: A => CompileTime[B]): CompileTime[A] =
      flatMap(a => f(a).map(_ => a))
  }

  object CompileTime {

    def pure[F[_], A](a: A): CompileTime[A] =
      CompileTime(state => (state, a))

    def current[F[_]]: CompileTime[CallMap] =
      CompileTime(state => (state, state))

    def traverse[F[_], A, B](list: List[A])(
        f: A => CompileTime[B]
    ): CompileTime[List[B]] = ???
  }

  private type Result[A] = CompileTime[Runtime[A]]
  private object Visitor extends Compilation.Visitor[Result] {
    def pure[A](a: A): Result[A] =
      CompileTime.pure(Runtime.Static(a))

    def getCached[F[_], A](
        compiler: Compiler[F],
        schema: Schema[A]
    ): Result[F[A]] =
      CompileTime.current[F].map((_: CallMap).get(compiler, schema)).flatMap {
        case Some(value) => CompileTime.pure(value)
        case None =>
          compiler(schema).visit(Visitor).flatTap { eval =>
            CompileTime { (callMap: CallMap) =>
              callMap.get(compiler, schema) match {
                case Some(_) => (callMap, eval)
                case None =>
                  (callMap.add(compiler, schema, eval), eval)
              }
            }
          }
      }

    def cyclic[F[_], A](
        compiler: Compiler[F],
        lazySchema: Lazy[Schema[A]],
        make: (() => F[A]) => F[A]
    ): Result[F[A]] = {
      val wrapped = Schema.LazySchema(lazySchema)
      val memoized = lazySchema.value
      val dynamicCall =
        Runtime.Dynamic { (callMap: CallMap) =>
          lazy val compiled =
            callMap.get(compiler, memoized).getOrElse(???).run(callMap)
          make(() => compiled)
        }
      val recursive = compiler(memoized).visit(Visitor)
      CompileTime { callMap =>
        callMap.get(compiler, wrapped) match {
          case None =>
            val phase1Map = callMap.add(compiler, wrapped, dynamicCall)
            val (phase2Map, phase2Res) = recursive.run(phase1Map)
            (phase2Map.add(compiler, memoized, phase2Res), phase2Res)
          case Some(value) =>
            (callMap, value)
        }
      }
    }

    def traverse[A, B](list: List[A], f: A => Result[B]): Result[List[B]] =
      CompileTime.traverse(list)(f).map { Runtime.sequence(_) }

    def map[A, B](list: Result[A], f: A => B): Result[B] =
      list.map(_.map(f))
  }

  sealed trait Runtime[A] {
    def map[B](f: A => B): Runtime[B] = this match {
      case Runtime.Static(a)    => Runtime.Static(f(a))
      case Runtime.Dynamic(run) => Runtime.Dynamic(run.andThen(f))
    }

    def run(callMap: CallMap): A = this match {
      case Runtime.Static(fa)   => fa
      case Runtime.Dynamic(run) => run(callMap)
    }
  }

  object Runtime {
    type Of[F[_], A] = Runtime[F[A]]
    private[Compiler] case class Static[A](a: A) extends Runtime[A]
    private[Compiler] case class Dynamic[A](run: CallMap => A)
        extends Runtime[A]

    def sequence[F[_], A](list: List[Runtime[A]]): Runtime[List[A]] = {
      // Small optimisation : if all are static, we create a static one.
      val statics = list.collect { case Static(a) => a }
      if (statics.size == list.size) Runtime.Static(statics)
      else Runtime.Dynamic { callMap => list.map(_.run(callMap)) }
    }
  }

  class CallMap(
      val map: Map[Any, Any]
  ) {
    def add[F[_], A](
        compiler: Compiler[F],
        schema: Schema[A],
        fa: Runtime[F[A]]
    ): CallMap = new CallMap(
      map + (CallMap.Key(compiler, schema) -> fa)
    )

    def get[F[_], A](
        tag: Compiler[F],
        schema: Schema[A]
    ): Option[Runtime[F[A]]] =
      map.get(schema).asInstanceOf[Option[Runtime[F[A]]]]

    def size: Int = map.size
    override def toString(): String = map.toString()

    def prune: CallMap = new CallMap(
      map.view.filter {
        case (_, Runtime.Dynamic(_)) => true
        case _                       => false
      }.toMap
    )

  }

  object CallMap {
    case class Key[F[_], A](tag: Compiler[F], schema: Schema[A])
    def empty: CallMap = new CallMap(Map.empty)
  }

}
