package smithy4s.example

import smithy.api.BigDecimal
import smithy.api.Timestamp
import smithy4s.Hints
import smithy4s.Schema
import smithy4s.ShapeId
import smithy4s.ShapeTag
import smithy4s.schema.Schema.int
import smithy4s.schema.Schema.string
import smithy4s.schema.Schema.struct

final case class TimeStructure(localDate: Option[smithy.api.String] = None, localDateTime: Option[java.lang.String] = None, localTime: Option[smithy.api.String] = None, offsetDateTime: Option[Timestamp] = None, offsetTime: Option[java.lang.String] = None, zoneId: Option[java.lang.String] = None, zoneOffset: Option[java.lang.String] = None, zonedDateTime: Option[java.lang.String] = None, year: Option[Int] = None, yearMonth: Option[java.lang.String] = None, monthDay: Option[java.lang.String] = None, duration: Option[BigDecimal] = None)

object TimeStructure extends ShapeTag.Companion[TimeStructure] {
  val id: ShapeId = ShapeId("smithy4s.example", "TimeStructure")

  val hints: Hints = Hints.empty

  // constructor using the original order from the spec
  private def make(localDate: Option[smithy.api.String], localDateTime: Option[java.lang.String], localTime: Option[smithy.api.String], offsetDateTime: Option[Timestamp], offsetTime: Option[java.lang.String], zoneId: Option[java.lang.String], zoneOffset: Option[java.lang.String], zonedDateTime: Option[java.lang.String], year: Option[Int], yearMonth: Option[java.lang.String], monthDay: Option[java.lang.String], duration: Option[BigDecimal]): TimeStructure = TimeStructure(localDate, localDateTime, localTime, offsetDateTime, offsetTime, zoneId, zoneOffset, zonedDateTime, year, yearMonth, monthDay, duration)

  implicit val schema: Schema[TimeStructure] = struct[TimeStructure](
    smithy.api.String.schema.optional[TimeStructure]("localDate", _.localDate).addHints(Hints.dynamic(ShapeId("alloy", "dateFormat"), smithy4s.Document.obj())),
    string.optional[TimeStructure]("localDateTime", _.localDateTime).addHints(Hints.dynamic(ShapeId("alloy", "localDateTimeFormat"), smithy4s.Document.obj())),
    smithy.api.String.schema.optional[TimeStructure]("localTime", _.localTime).addHints(Hints.dynamic(ShapeId("alloy", "localTimeFormat"), smithy4s.Document.obj())),
    Timestamp.schema.optional[TimeStructure]("offsetDateTime", _.offsetDateTime).addHints(Hints.dynamic(ShapeId("alloy", "offsetDateTimeFormat"), smithy4s.Document.obj()), Hints.dynamic(ShapeId("smithy.api", "timestampFormat"), smithy4s.Document.fromString("date-time"))),
    string.optional[TimeStructure]("offsetTime", _.offsetTime).addHints(Hints.dynamic(ShapeId("alloy", "offsetTimeFormat"), smithy4s.Document.obj())),
    string.optional[TimeStructure]("zoneId", _.zoneId).addHints(Hints.dynamic(ShapeId("alloy", "zoneIdFormat"), smithy4s.Document.obj())),
    string.optional[TimeStructure]("zoneOffset", _.zoneOffset).addHints(Hints.dynamic(ShapeId("alloy", "zoneOffsetFormat"), smithy4s.Document.obj())),
    string.optional[TimeStructure]("zonedDateTime", _.zonedDateTime).addHints(Hints.dynamic(ShapeId("alloy", "zonedDateTimeFormat"), smithy4s.Document.obj())),
    int.optional[TimeStructure]("year", _.year).addHints(Hints.dynamic(ShapeId("alloy", "yearFormat"), smithy4s.Document.obj())),
    string.optional[TimeStructure]("yearMonth", _.yearMonth).addHints(Hints.dynamic(ShapeId("alloy", "yearMonthFormat"), smithy4s.Document.obj())),
    string.optional[TimeStructure]("monthDay", _.monthDay).addHints(Hints.dynamic(ShapeId("alloy", "monthDayFormat"), smithy4s.Document.obj())),
    BigDecimal.schema.optional[TimeStructure]("duration", _.duration).addHints(Hints.dynamic(ShapeId("alloy", "durationSecondsFormat"), smithy4s.Document.obj())),
  )(make).withId(id).addHints(hints)
}
