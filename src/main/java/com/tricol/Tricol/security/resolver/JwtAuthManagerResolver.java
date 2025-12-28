package com.tricol.Tricol.security.resolver;

import com.nimbusds.jwt.JWTParser;
import com.tricol.Tricol.security.converter.JwtAuthoritiesConverter;
import com.tricol.Tricol.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.text.ParseException;

@RequiredArgsConstructor
public class JwtAuthManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {

    @Value("${jwt.issuer}")
    private String localIssuer;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String keycloakIssuer;

    @Qualifier("localJwtDecoder")
    private JwtDecoder localJwtDecoder;
    @Qualifier("keycloakJwtDecoder")
    private JwtDecoder keycloakJwtDecoder;

    private final JwtUtil jwtUtil;
    private final JwtAuthoritiesConverter jwtAuthoritiesConverter;

    @Override
    public AuthenticationManager resolve(HttpServletRequest request) {
        String token = extractToken(request);

        if(token == null){
            return null;
        }

        String issuer = extractIssuer(token);
        return null;

    }

    private String extractToken(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");

        if(bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String extractIssuer(String token){
        String[] tokenParts = token.split("\\.");
        if(tokenParts.length < 2){
            throw new IllegalArgumentException("invalid jwt format !");
        }

        try{
            return JWTParser.parse(token).getJWTClaimsSet().getIssuer();
        } catch (ParseException parseException){
            return "invalid jwt";
        }
    }



}
