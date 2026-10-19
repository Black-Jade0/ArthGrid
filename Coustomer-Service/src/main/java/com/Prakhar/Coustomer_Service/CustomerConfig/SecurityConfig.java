package com.Prakhar.Coustomer_Service.CustomerConfig;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * true  (default) -> every request except signup needs an Auth0 JWT ("Bearer ..." header).
     * false (local)   -> everything is open, so you can test with Postman/curl without Auth0.
     * Set with the SECURITY_ENABLED environment variable.
     */
    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (!securityEnabled) {
            System.out.println("[LOCAL MODE] Security is OFF - all endpoints are open, no JWT needed.");
            http.csrf(csrf -> csrf.disable())
                    .cors(cors -> cors.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        http
                // 1. CSRF disable rehna chahiye kyunki hum API bana rahe hain
                .csrf(csrf -> csrf.disable())

                // 2. IMPORTANT: Customer Service ka apna CORS disable rakho
                // taaki sirf Gateway ki settings chalein
                .cors(cors -> cors.disable())

                .authorizeHttpRequests(auth -> auth
                        // 3. OPTIONS request ko sabse pehle allow karo (Preflight fix)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 4. POST request for onboarding (Public)
                        .requestMatchers(HttpMethod.POST, "/api/v1/customers").permitAll()

                        // 5. Baaki sab authenticated
                        .anyRequest().authenticated()
                )
                // 6. OAuth2 Resource Server setup sahi hai
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));

        return http.build();
    }


}
