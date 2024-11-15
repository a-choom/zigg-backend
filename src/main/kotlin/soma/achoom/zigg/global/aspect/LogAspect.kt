package soma.achoom.zigg.global.aspect

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import lombok.extern.slf4j.Slf4j
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.*
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import kotlin.time.measureTime

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

//    @Around("service()||repository()")
//    private fun logging(joinPoint: ProceedingJoinPoint): Any? {
//        var result: Any?
//        val timeMs = measureTime {
//            result = joinPoint.proceed()
//        }.inWholeMilliseconds
//        log.info("log = {}, time = {}ms", joinPoint.signature,timeMs)
//        return result
//    }

    @Before("controller()")
    private fun logRequestBody() {
        val requestBody = try {
            request.reader.use { it.readText() }
        } catch (e: Exception) {
            null
        }
        log.info("Request endpoint: "+ request.requestURI)
        request.headerNames?.asIterator()?.forEachRemaining{
            headerName ->
            log.info("$headerName : ${request.getHeader(headerName)}")
        }

        val req = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes

        if (!requestBody.isNullOrEmpty()) {
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

    @AfterThrowing("all()", throwing = "exception")
    fun exceptionThrowingLogger(joinPoint: JoinPoint, exception: Exception) {
        log.error("An exception has been thrown in ${joinPoint.signature.name}()", exception)
    }

//    @After("controller()")
//    fun controllerResponseLogger(joinPoint: JoinPoint) {
//        val methodSignature = joinPoint.signature as MethodSignature
//        val method = methodSignature.method
//        log.info("Request : ${method.name}")
//    }
}
