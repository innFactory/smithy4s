
package refinements.validated
import java.time.LocalDate

object Main extends App {
  println(scripted.date.StructWithDate(LocalDate.parse("2012-12-03")))
}
