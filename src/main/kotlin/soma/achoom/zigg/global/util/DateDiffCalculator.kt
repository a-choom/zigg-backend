package soma.achoom.zigg.global.util

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class DateDiffCalculator {
    companion object{
        fun calculateDateDiffByDate(date1 : LocalDateTime , date2: LocalDateTime) : Long {
            val daysBetween: Long = ChronoUnit.DAYS.between(date1.toLocalDate(), date2.toLocalDate())
            return abs(daysBetween)
        }
    }
}