package com.tricol.Tricol.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;

@Configuration
public class JwtDecoderConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwtSetUri;

    @Value("${jwt.issuer}")
    private String localIssuer;

    @Bean
    @Qualifier("localJwtDecoder")
    public JwtDecoder localJwtDecoder(){
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).build();
        decoder.setJwtValidator(new JwtIssuerValidator(localIssuer));
        return decoder;
    }

    @Bean
    @Qualifier("keycloakJwtDecoder")
    public JwtDecoder keycloakJwtDecoder(){
        return NimbusJwtDecoder.withJwkSetUri(jwtSetUri).build();
    }

}
