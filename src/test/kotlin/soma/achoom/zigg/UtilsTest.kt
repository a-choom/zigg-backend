package soma.achoom.zigg

import soma.achoom.zigg.global.util.DateDiffCalculator
import java.time.LocalDateTime
import kotlin.test.Test

class UtilsTest {
    @Test
    fun `date diff calculator test`(){
        assert(DateDiffCalculator.calculateDateDiffByDate(LocalDateTime.now(), LocalDateTime.now().minusDays(7)) == 7L)
    }
}