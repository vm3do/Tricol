package com.tricol.Tricol.security.filter;

import com.tricol.Tricol.security.converter.JwtAuthoritiesConverter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class KeycloakJwtFilter extends OncePerRequestFilter {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String keycloakIssuer;

    @Qualifier("keycloakJwtDecoder")
    private final JwtDecoder keycloakJwtDecoder;

    private final JwtAuthoritiesConverter jwtAuthoritiesConverter;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Jwt jwt = keycloakJwtDecoder.decode(token);

            String issuer = jwt.getIssuer().toString();
            if (!keycloakIssuer.equals(issuer)) {
                filterChain.doFilter(request, response);
                return;
            }

            Collection<GrantedAuthority> authorities = jwtAuthoritiesConverter.convert(jwt);
            JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
            return;

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        }
    }
}

