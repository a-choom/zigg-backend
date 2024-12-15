package soma.achoom.zigg

import soma.achoom.zigg.global.util.DateDiffCalculator
import soma.achoom.zigg.global.util.S3UrlParser
import java.time.LocalDateTime
import kotlin.test.Test

class UtilsTest {
    @Test
    fun `date diff calculator test`(){
        assert(DateDiffCalculator.calculateDateDiffByDate(LocalDateTime.now(), LocalDateTime.now().minusDays(7)) == 7L)
    }
    @Test
    fun `s3 url parser test`(){
        assert(S3UrlParser.extractionKeyFromUrl("https://test.test.com/test/test/4sdzxv123.mp4?test=test") == "test/test/4sdzxv123.mp4")
        println( S3UrlParser.extractionKeyFromUrl("https://test.test.com/test/test/4sdzxv123.mp4?test=test"))
    }
}