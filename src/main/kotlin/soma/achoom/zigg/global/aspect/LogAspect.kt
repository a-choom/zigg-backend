package soma.achoom.zigg.global.aspect

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import lombok.extern.slf4j.Slf4j
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.util.ContentCachingRequestWrapper

@Aspect
@Slf4j
@Component
class LogAspect(
    private val request: HttpServletRequest,
    private val objectMapper: ObjectMapper
)  {
    
    val log = LoggerFactory.getLogger(this.javaClass)

    @Pointcut("execution(* soma.achoom.zigg..*(..))")
    fun all() {

    }

    @Pointcut("execution(* soma.achoom.zigg.*.service.*.*(..))")
    fun service() {

    }

    @Pointcut("execution(* soma.achoom.zigg.*.controller.*.*(..))")
    fun controller() {

    }

    @Pointcut("execution(* soma.achoom.zigg.*.repository.*.*(..))")
    fun repository() {

    }

    @Around("controller()")
    private fun logRequestAndResponse(joinPoint: ProceedingJoinPoint) : Any{
        logRequest()
        val result = joinPoint.proceed()
        return logResponse(result)
    }


    private fun logRequest() {

        val req = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request as ContentCachingRequestWrapper

        // 읽기 가능한 바이트 배열에서 요청 바디를 가져옴
        val requestBody = String(req.contentAsByteArray, Charsets.UTF_8)

        val headersMap = mutableMapOf<String, String>()

        request.headerNames?.asIterator()?.forEachRemaining { headerName ->
            headersMap[headerName] = request.getHeader(headerName)
        }

        val headersJson = try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(headersMap)
        } catch (e: Exception) {
            log.warn("Failed to convert headers to JSON", e)
            "{}"
        }

        log.info("Request Headers: \n$headersJson")
        log.info("Request endpoint: \n${request.requestURI}")

        if (requestBody.isNotEmpty()) {
            try {
                val json = objectMapper.readValue(requestBody, Any::class.java)
                val formattedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json)
                log.info("Request Body:\n$formattedJson")
            } catch (e: Exception) {
                log.warn("Invalid JSON in Request:\n$requestBody")
            }
        } else {
            log.info("Request Body is empty.")
        }
    }
    private fun logResponse(result:Any): Any{
        return try {
            val json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result)
            log.info("Response Body:\n$json")
            result
        } catch (e: Exception) {
            log.warn("Failed to log response body", e)
            result
        }
    }


    @AfterThrowing("all()", throwing = "exception")
    fun exceptionThrowingLogger(joinPoint: JoinPoint, exception: Exception) {
        log.error("An exception has been thrown in ${joinPoint.signature.name}()", exception)
        try {
            joinPoint.args.forEach {
                val json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(it)
                log.info(json)
            }
        }catch (e:Exception){
            log.warn("Failed to log args",e)
        }

    }
}
