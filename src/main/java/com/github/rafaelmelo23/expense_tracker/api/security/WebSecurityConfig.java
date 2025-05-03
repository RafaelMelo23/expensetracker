package com.github.rafaelmelo23.expense_tracker.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
public class WebSecurityConfig {

    private final JWTFilterSecurity jwtFilterSecurity;

    public WebSecurityConfig(JWTFilterSecurity jwtFilterSecurity) {
        this.jwtFilterSecurity = jwtFilterSecurity;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //        http.cors(cors -> cors.configurationSource(request -> {
//            CorsConfiguration config = new CorsConfiguration();
//            config.setAllowCredentials(true);
//            config.addAllowedOrigin(*);
//            config.addAllowedMethod("*");
//            config.addAllowedHeader("*");
//            return config;
//        }));

        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
        http.csrf((csrf) -> csrf.disable());
        //Disabling iframe + cors temporarily

        http.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(jwtFilterSecurity, AuthorizationFilter.class);
        http.authorizeHttpRequests(authorize -> authorize

//                .requestMatchers("/admin/**").hasRole("ADMIN")

                // Authenticated only API mappings
                .requestMatchers("/api/expense/**").authenticated()

                // Public API mappings
                .requestMatchers(
                                "/api/user/**").permitAll()

                // Html/Static public mappings
                .requestMatchers("/js/**",
                                "/css/**",
                                "/login",
                                "/register",
                                "/").permitAll()

                        .anyRequest().authenticated()
        );

        return http.build();
    }
}
