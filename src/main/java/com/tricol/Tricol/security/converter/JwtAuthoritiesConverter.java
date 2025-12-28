package com.tricol.Tricol.security.converter;

import com.tricol.Tricol.model.Permission;
import com.tricol.Tricol.model.RoleApp;
import com.tricol.Tricol.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Value("${jwt.issuer}")
    private String localIssuer;
    private final RoleRepository roleRepository;

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {

        String issuer = jwt.getIssuer().toString();

        if(localIssuer.equals(issuer)) {
            return extractLocalAuthorities(jwt);
        } else {
            return extractKeycloakAuthorities(jwt);
        }
    }

    private Collection<GrantedAuthority> extractLocalAuthorities(Jwt jwt){
        List<String> authorities = jwt.getClaimAsStringList("authorities");

        if(authorities == null){
            return Collections.emptyList();
        }

        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    private Collection<GrantedAuthority> extractKeycloakAuthorities(Jwt jwt){

        Set<String> roles = new HashSet<>();
        Set<GrantedAuthority> authorities = new HashSet<>();

        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if(resourceAccess != null && resourceAccess.containsKey("tricol-client")){

            Map<String, Object> tricolClient = (Map<String, Object>) resourceAccess.get("tricol-client");

            if(tricolClient != null && tricolClient.containsKey("roles")){
                List<String> realmRoles = (List<String>) tricolClient.get("roles");
                    if (realmRoles != null) {
                        roles.addAll(realmRoles);
                    }
            }
        }

        for (String roleName : roles){
            Optional<RoleApp> roleOpt = roleRepository.findByName(roleName);

            if (roleOpt.isPresent()){
                RoleApp role = roleOpt.get();

                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                Set<Permission> permissions = role.getPermissions();

                for(Permission permission : permissions){
                    authorities.add(new SimpleGrantedAuthority(permission.getName()));
                }
            }
        }

        return authorities;
    }
}
