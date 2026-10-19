package com.Prakhar.Coustomer_Service.CustomerConfig;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

    private static final Logger log = LoggerFactory.getLogger(FeignClientConfig.class);

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String authHeader = request.getHeader("Authorization");

                    if (authHeader != null) {
                        requestTemplate.header("Authorization", authHeader);
                        log.info("Token successfully forwarded to internal service");
                    } else {
                        log.warn("No Authorization header found in the incoming request");
                    }
                } else {
                    log.error("RequestContextHolder is null. Possible thread issue or running outside web request scope.");
                }
            }
        };
    }
}