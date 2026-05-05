$version: "2"

namespace smithy4s.example

use alloy#LocalDate
use alloy#LocalTime
use alloy#OffsetDateTime
use alloy#Duration
use alloy#dateFormat
use alloy#offsetDateTimeFormat
use alloy#localTimeFormat
use alloy#durationSecondsFormat
use alloy#localDateTimeFormat
use alloy#offsetTimeFormat
use alloy#zoneIdFormat
use alloy#zoneOffsetFormat
use alloy#zonedDateTimeFormat
use alloy#yearFormat
use alloy#yearMonthFormat
use alloy#monthDayFormat

@dateFormat
string MyLocalDate

@localTimeFormat
string MyLocalTime

@durationSecondsFormat
bigDecimal MyDuration

@offsetDateTimeFormat
@timestampFormat("date-time")
timestamp MyOffsetDateTime

structure LocalDateStructure {
    @required
    localDate: LocalDate
    @required
    localDate2: MyLocalDate
}

structure LocalTimeStructure {
    @required
    localTime: LocalTime
    @required
    localTime2: MyLocalTime
}


structure DurationStructure {
    @required
    duration: Duration
    @required
    duration2: MyDuration
}

structure OffsetDateTimeStructure {
    @required
    offsetDateTime: OffsetDateTime
    @required
    offsetDateTime2: MyOffsetDateTime
}

structure TimeStructure {
    @dateFormat
    localDate: String
    @localDateTimeFormat
    localDateTime: String
    @localTimeFormat
    localTime: String
    @offsetDateTimeFormat
    @timestampFormat("date-time")
    offsetDateTime: Timestamp
    @offsetTimeFormat
    offsetTime: String
    @zoneIdFormat
    zoneId: String
    @zoneOffsetFormat
    zoneOffset: String
    @zonedDateTimeFormat
    zonedDateTime: String
    @yearFormat
    year: Integer
    @yearMonthFormat
    yearMonth: String
    @monthDayFormat
    monthDay: String
    @durationSecondsFormat
    duration: BigDecimal
}
