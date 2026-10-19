package com.Prakhar.Apigateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        corsConfig.setAllowedOriginPatterns(Arrays.asList(
                "https://agrawal-bankui.vercel.app",
                "http://localhost:3000" // Testing ke liye
        ));
        // 1. Sabhi Origins ko allow karo (Vercel ke liye)
        corsConfig.addAllowedOriginPattern("*");

        // 2. Saare Methods allow karo (POST, GET, OPTIONS, etc.)
        corsConfig.addAllowedMethod("*");

        // 3. Saare Headers allow karo
        corsConfig.addAllowedHeader("*");

        // 4. Credentials allow karo (Cookies/Tokens ke liye)
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}