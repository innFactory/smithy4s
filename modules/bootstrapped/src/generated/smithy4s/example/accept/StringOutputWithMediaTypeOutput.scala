package smithy4s.example.accept

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.struct

/** @param result
  *   Plain text payload type
  */
final case class StringOutputWithMediaTypeOutput(result: Option[PlainTextPayload] = None)

object StringOutputWithMediaTypeOutput extends ShapeTag.Companion[StringOutputWithMediaTypeOutput] {
  val id: ShapeId = ShapeId("smithy4s.example.accept", "StringOutputWithMediaTypeOutput")

  val hints: Hints = Hints(
    smithy.api.Output(),
  ).lazily

  // constructor using the original order from the spec
  private def make(result: Option[PlainTextPayload]): StringOutputWithMediaTypeOutput = StringOutputWithMediaTypeOutput(result)

  implicit val schema: Schema[StringOutputWithMediaTypeOutput] = struct(
    PlainTextPayload.schema.optional[StringOutputWithMediaTypeOutput]("result", _.result).addHints(smithy.api.HttpPayload()),
  )(make).withId(id).addHints(hints)
}
