package soma.achoom.zigg.global.aspect
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.Filter
import org.springframework.web.util.ContentCachingRequestWrapper

class CachingRequestFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val wrappedRequest = if (request is HttpServletRequest) {
            ContentCachingRequestWrapper(request)
        } else {
            request
        }
        chain.doFilter(wrappedRequest, response)
    }
}