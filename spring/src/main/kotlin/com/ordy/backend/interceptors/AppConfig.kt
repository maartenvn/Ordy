package com.ordy.backend.interceptors

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class AppConfig: WebMvcConfigurer {

    @Autowired
    private lateinit var authInterceptor: AuthInterceptor

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(listOf(
                        "/groups/**",
                        "/user/**",
                        "/locations/**",
                        "/orders/**"
                ))
                .excludePathPatterns("/auth/**")
    }
}