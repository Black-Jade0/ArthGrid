package com.Prakhar.Payment_Service.Config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

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
                        System.out.println("Token forwarded from Payment Service");
                    } else {
                        // Else block 1: Header nahi mila
                        System.out.println("Token NOT forwarded: No Authorization header found in the incoming request.");
                    }
                } else {
                    // Else block 2: Context nahi mila
                    System.out.println("Token NOT forwarded: RequestContextHolder is null.");
                }
            }
        };
    }
}