package smithy4s.example

import smithy.api.String
import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.localdate
import smithy4s.schema.Schema.struct
import smithy4s.time.LocalDate

final case class LocalDateStructure(localDate: LocalDate, localDate2: MyLocalDate, localDate3: String)

object LocalDateStructure extends ShapeTag.Companion[LocalDateStructure] {
  val id: ShapeId = ShapeId("smithy4s.example", "LocalDateStructure")

  val hints: Hints = Hints.empty

  // constructor using the original order from the spec
  private def make(localDate: LocalDate, localDate2: MyLocalDate, localDate3: String): LocalDateStructure = LocalDateStructure(localDate, localDate2, localDate3)

  implicit val schema: Schema[LocalDateStructure] = struct[LocalDateStructure](
    localdate.required[LocalDateStructure]("localDate", _.localDate),
    MyLocalDate.schema.required[LocalDateStructure]("localDate2", _.localDate2),
    String.schema.required[LocalDateStructure]("localDate3", _.localDate3).addHints(Hints.dynamic(ShapeId("alloy", "dateFormat"), smithy4s.Document.obj())),
  )(make).withId(id).addHints(hints)
}
