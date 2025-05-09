package com.github.rafaelmelo23.expense_tracker.api.security;

import jakarta.servlet.http.HttpServletResponse;
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
                .requestMatchers("/api/expense/**",
                                 "/api/additions/**"
                        ).authenticated()

                // Authenticated Html/Static mappings
                .requestMatchers("/first/registry",
                                 "/calendar"
                        ).authenticated()

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

        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response,
                             authException) -> {
                    String accept = request.getHeader("Accept");
                    if (accept != null && accept.contains("text/html")) {
                        response.sendRedirect("/login");
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                    }
                })
        );

        return http.build();
    }
}
