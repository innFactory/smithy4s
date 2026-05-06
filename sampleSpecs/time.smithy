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
    @dateFormat
    @required
    localDate3: String
}

structure LocalTimeStructure {
    @required
    localTime: LocalTime
    @required
    localTime2: MyLocalTime
    @localTimeFormat
    @required
    localTime3: String
}


structure DurationStructure {
    @required
    duration: Duration
    @required
    duration2: MyDuration
    @durationSecondsFormat
    @required
    duration3: Duration
}

structure OffsetDateTimeStructure {
    @required
    offsetDateTime: OffsetDateTime
    @required
    offsetDateTime2: MyOffsetDateTime
    @offsetDateTimeFormat
    @timestampFormat("date-time")
    @required
    offsetDateTime3: Timestamp
}
