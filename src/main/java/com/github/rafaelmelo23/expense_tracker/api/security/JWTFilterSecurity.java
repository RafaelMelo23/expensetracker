package com.github.rafaelmelo23.expense_tracker.api.security;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.model.dao.LocalUserDAO;
import com.github.rafaelmelo23.expense_tracker.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JWTFilterSecurity extends OncePerRequestFilter implements ChannelInterceptor {

    private final JWTService jwtService;
    private final LocalUserDAO localUserDAO;
    @Value("${jwt.prometheus.user.email}")
    private String prometheusEmail;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        String jwtToken = extractToken(request);

        try {
            UsernamePasswordAuthenticationToken authToken = checkTokenAndAuth(jwtToken);

            if (jwtToken != null) {
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);
        } catch (JWTDecodeException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unvalid or expired token");
        }

    }

    private UsernamePasswordAuthenticationToken checkTokenAndAuth(String token) {

        if (token != null) {
            try {
                if (jwtService.isTokenExpired(token)) {
                    throw new JWTVerificationException("Expired JWT token");
                }

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                String email = jwtService.getEmailFromToken(token);
                String role = jwtService.getRoleFromToken(token);

                if (!role.startsWith("ROLE_")) {
                    role = "ROLE_" + role;
                }
                authorities.add(new SimpleGrantedAuthority(role));

                Object principal;

                if (Objects.equals(email, prometheusEmail)) {
                    principal = email;
                } else {
                    LocalUser user = localUserDAO.findByEmailIgnoreCase(email)
                            .orElseThrow(() -> new JWTVerificationException("User with email " + email + " not found"));
                    principal = user;
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, token, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                return authentication;
            } catch (JWTDecodeException e) {

                return null;
            }
        }
        return null;
    }

    private String extractToken(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (("JWT").equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
