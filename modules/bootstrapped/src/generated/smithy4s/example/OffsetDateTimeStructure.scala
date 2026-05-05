package smithy4s.example

import smithy.api.Timestamp
import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.offsetdatetime
import smithy4s.schema.Schema.struct
import smithy4s.time.OffsetDateTime

final case class OffsetDateTimeStructure(offsetDateTime: OffsetDateTime, offsetDateTime2: MyOffsetDateTime, offsetDateTime3: Timestamp)

object OffsetDateTimeStructure extends ShapeTag.Companion[OffsetDateTimeStructure] {
  val id: ShapeId = ShapeId("smithy4s.example", "OffsetDateTimeStructure")

  val hints: Hints = Hints.empty

  // constructor using the original order from the spec
  private def make(offsetDateTime: OffsetDateTime, offsetDateTime2: MyOffsetDateTime, offsetDateTime3: Timestamp): OffsetDateTimeStructure = OffsetDateTimeStructure(offsetDateTime, offsetDateTime2, offsetDateTime3)

  implicit val schema: Schema[OffsetDateTimeStructure] = struct[OffsetDateTimeStructure](
    offsetdatetime.required[OffsetDateTimeStructure]("offsetDateTime", _.offsetDateTime),
    MyOffsetDateTime.schema.required[OffsetDateTimeStructure]("offsetDateTime2", _.offsetDateTime2),
    Timestamp.schema.required[OffsetDateTimeStructure]("offsetDateTime3", _.offsetDateTime3).addHints(Hints.dynamic(ShapeId("alloy", "offsetDateTimeFormat"), smithy4s.Document.obj()), Hints.dynamic(ShapeId("smithy.api", "timestampFormat"), smithy4s.Document.fromString("date-time"))),
  )(make).withId(id).addHints(hints)
}
