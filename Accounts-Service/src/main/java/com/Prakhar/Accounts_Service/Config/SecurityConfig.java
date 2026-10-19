package com.Prakhar.Accounts_Service.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * true  (default) -> deposit/withdraw/holds need an Auth0 JWT ("Bearer ..." header).
     * false (local)   -> everything is open, so you can test with Postman/curl without Auth0.
     * Set with the SECURITY_ENABLED environment variable.
     */
    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (!securityEnabled) {
            System.out.println("[LOCAL MODE] Security is OFF - all endpoints are open, no JWT needed.");
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        http
                .csrf(csrf -> csrf.disable()) // CSRF band karna zaroori hai POST requests ke liye
                .authorizeHttpRequests(auth -> auth
                        // YAHAN HAI MAGIC LINE:
                        // Iska matlab: "Agar koi POST request /api/v1/accounts par aaye, to bina token ke aane do"
                        .requestMatchers(HttpMethod.POST, "/api/v1/accounts").permitAll()

                        // Baaki kisi bhi kaam ke liye (Deposit/Withdraw) Login (Token) zaroori hai
                        .anyRequest().authenticated()
                )
                // Agar tum OAuth2/Resource Server use kar rahe ho to ye line rehne do,
                // nahi to ise hata sakte ho agar abhi sirf basic testing kar rahe ho.
                // Lekin 401 aa raha tha matlab security dependency hai, to ye safe hai.
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
