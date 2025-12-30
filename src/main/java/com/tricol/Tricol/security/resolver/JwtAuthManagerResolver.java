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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class JwtAuthManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {

    @Value("${jwt.issuer}")
    private String localIssuer;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String keycloakIssuer;

    @Qualifier("localJwtDecoder")
    private final JwtDecoder localJwtDecoder;
    @Qualifier("keycloakJwtDecoder")
    private final JwtDecoder keycloakJwtDecoder;

    private final JwtAuthoritiesConverter jwtAuthoritiesConverter;

    @Override
    public AuthenticationManager resolve(HttpServletRequest request) {
        String token = extractToken(request);

        if(token == null){
            System.out.println("⚠️ DEBUG: No Bearer token found in request");
            return null;
        }

        String issuer = extractIssuer(token);

        // TEMPORARY DEBUG - REMOVE LATER
        System.out.println("🔍 DEBUG: Token issuer: '" + issuer + "'");
        System.out.println("🔍 DEBUG: Expected local issuer: '" + localIssuer + "'");
        System.out.println("🔍 DEBUG: Keycloak issuer: '" + keycloakIssuer + "'");
        System.out.println("🔍 DEBUG: Match with local: " + issuer.equals(localIssuer));

        if(issuer.equals(localIssuer)){
            System.out.println("✅ DEBUG: Routing to LOCAL decoder");
            return createAuthManager(localJwtDecoder);
        } else {
            System.out.println("✅ DEBUG: Routing to KEYCLOAK decoder");
            return createAuthManager(keycloakJwtDecoder);
        }

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

    private AuthenticationManager createAuthManager(JwtDecoder decoder){
        // had provider kaydecodi, ivalider signature + claims & ysweb autnetication object;
        // decode using our decoder
        //validate
        //convert decoded jwt to authentication object
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);

        //jwt is the decoded token
        provider.setJwtAuthenticationConverter(jwt -> {
            Collection<GrantedAuthority> authorities =
                    jwtAuthoritiesConverter.convert(jwt);
            //authentication object with authorities
            return new JwtAuthenticationToken(jwt, authorities);
        });

        // TEMPORARY DEBUG - Wrap to catch exact error
        return authentication -> {
            try {
                System.out.println("🔍 DEBUG: Attempting to authenticate with decoder...");
                return provider.authenticate(authentication);
            } catch (Exception e) {
                System.out.println("❌ DEBUG: Authentication failed!");
                System.out.println("❌ DEBUG: Error type: " + e.getClass().getName());
                System.out.println("❌ DEBUG: Error message: " + e.getMessage());
                if (e.getCause() != null) {
                    System.out.println("❌ DEBUG: Caused by: " + e.getCause().getClass().getName());
                    System.out.println("❌ DEBUG: Cause message: " + e.getCause().getMessage());
                }
                throw e;
            }
        };

    }



}
