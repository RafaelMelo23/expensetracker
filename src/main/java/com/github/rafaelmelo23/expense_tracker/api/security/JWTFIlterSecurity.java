package com.github.rafaelmelo23.expense_tracker.api.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.github.rafaelmelo23.expense_tracker.dao.LocalUserDAO;
import com.github.rafaelmelo23.expense_tracker.model.LocalUser;
import com.github.rafaelmelo23.expense_tracker.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
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

@Component
@RequiredArgsConstructor
public class JWTFIlterSecurity extends OncePerRequestFilter implements ChannelInterceptor {

    private final JWTService jwtService;
    private final LocalUserDAO localUserDAO;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        String jwtToken = extractTokenFromHttpCookie(request);

        try {
            UsernamePasswordAuthenticationToken authToken = checkTokenAndAuth(jwtToken);

            if (jwtToken != null) {
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                return;
            }

            filterChain.doFilter(request, response);
        } catch (JWTVerificationException e) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or Expired JWT token");
        }
    }

    private UsernamePasswordAuthenticationToken checkTokenAndAuth(String token) {

        if (token != null) {
            try {
                if (jwtService.isTokenExpired(token)) {
                    throw new JWTVerificationException("Expired JWT token");
                }

                String email = jwtService.getEmailFromToken(token);
                LocalUser user = localUserDAO.findByEmailIgnoreCase(email)
                        .orElseThrow(() -> new JWTVerificationException("User with email " + email + " not found"));

                String role = jwtService.getRoleFromToken(token);

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                if (role.startsWith("ROLE_")) {
                    authorities.add(new SimpleGrantedAuthority(role));
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, token, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                return authentication;
            } catch (JWTVerificationException e) {
                throw new JWTVerificationException("Invalid/Expired JWT Token");
            }
        }
        return null;
    }

    private String extractTokenFromHttpCookie(HttpServletRequest request) {
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
