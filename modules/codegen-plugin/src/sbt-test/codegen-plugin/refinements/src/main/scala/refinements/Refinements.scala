package refinements.validated

import alloy.DateFormat

import java.time.LocalDate
import scala.util.Try
import smithy4s.{Refinement, RefinementProvider}


object Refinements {
  object dateFormat {
    private def newDate(value: String): Either[String, LocalDate] =
      Try(LocalDate.parse(value)).toEither.left.map(_.getMessage)

    object provider {
      implicit val forDate: RefinementProvider[DateFormat, String, LocalDate] =
        Refinement.drivenBy[DateFormat](newDate, _.toString)
    }
  }
}
