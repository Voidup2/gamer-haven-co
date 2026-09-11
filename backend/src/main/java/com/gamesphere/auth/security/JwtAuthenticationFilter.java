package com.gamesphere.auth.security;

import com.gamesphere.auth.repository.AuthSessionRepository;
import com.gamesphere.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final DatabaseUserDetailsService userDetailsService;
    private final AuthSessionRepository authSessionRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            DatabaseUserDetailsService userDetailsService,
            AuthSessionRepository authSessionRepository
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authSessionRepository = authSessionRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtService.parse(token);
                String username = claims.getSubject();
                Long userId = claims.get("userId", Long.class);
                String sessionIdValue = claims.get("sessionId", String.class);

                if (username != null && userId != null && sessionIdValue != null
                        && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UUID sessionId = UUID.fromString(sessionIdValue);
                    var session = authSessionRepository.findById(sessionId).orElseThrow();
                    if (!session.getUser().getId().equals(userId)
                            || !session.isActive(OffsetDateTime.now(ZoneOffset.UTC))) {
                        filterChain.doFilter(request, response);
                        return;
                    }

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    var authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(userId);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (RuntimeException ignored) {
                // Invalid, expired, or revoked tokens are treated as unauthenticated.
            }
        }

        filterChain.doFilter(request, response);
    }
}
