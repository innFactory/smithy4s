package smithy4s.example

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.string
import smithy4s.schema.Schema.struct

final case class SmokeTestOperationOutput(test: Option[String] = None)

object SmokeTestOperationOutput extends ShapeTag.Companion[SmokeTestOperationOutput] {
  val id: ShapeId = ShapeId("smithy4s.example", "SmokeTestOperationOutput")

  val hints: Hints = Hints(
    Hints.dynamic(ShapeId("smithy.api", "output"), smithy4s.Document.obj()),
  )

  // constructor using the original order from the spec
  private def make(test: Option[String]): SmokeTestOperationOutput = SmokeTestOperationOutput(test)

  implicit val schema: Schema[SmokeTestOperationOutput] = struct[SmokeTestOperationOutput](
    string.optional[SmokeTestOperationOutput]("test", _.test),
  )(make).withId(id).addHints(hints)
}
