package smithy4s.example.accept

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.string
import smithy4s.schema.Schema.struct

final case class StringOutputWithMediaTypeInput(data: Option[String] = None)

object StringOutputWithMediaTypeInput extends ShapeTag.Companion[StringOutputWithMediaTypeInput] {
  val id: ShapeId = ShapeId("smithy4s.example.accept", "StringOutputWithMediaTypeInput")

  val hints: Hints = Hints(
    smithy.api.Input(),
  ).lazily

  // constructor using the original order from the spec
  private def make(data: Option[String]): StringOutputWithMediaTypeInput = StringOutputWithMediaTypeInput(data)

  implicit val schema: Schema[StringOutputWithMediaTypeInput] = struct(
    string.optional[StringOutputWithMediaTypeInput]("data", _.data).addHints(smithy.api.HttpPayload()),
  )(make).withId(id).addHints(hints)
}
