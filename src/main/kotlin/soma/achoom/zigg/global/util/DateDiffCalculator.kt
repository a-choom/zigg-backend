package soma.achoom.zigg.global.util

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class DateDiffCalculator {
    companion object{
        fun calculateDateDiffByDate(after : LocalDateTime , before: LocalDateTime) : Long {
            val daysBetween: Long = ChronoUnit.DAYS.between(after.toLocalDate(), before.toLocalDate())
            return daysBetween
        }
    }
}