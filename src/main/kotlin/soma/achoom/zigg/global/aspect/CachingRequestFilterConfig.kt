package soma.achoom.zigg.global.aspect

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CachingRequestFilterConfig {
    @Bean
    fun cachingRequestFilter(): FilterRegistrationBean<CachingCustomRequestFilter>{
        val registrationBean = FilterRegistrationBean(CachingCustomRequestFilter())
        registrationBean.addUrlPatterns("/api/*")
        return registrationBean
    }
}