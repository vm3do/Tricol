package com.tricol.Tricol.security.service;

import com.tricol.Tricol.model.Permission;
import com.tricol.Tricol.model.RoleApp;
import com.tricol.Tricol.model.UserApp;
import com.tricol.Tricol.repository.UserRepository;
import com.tricol.Tricol.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserApp user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Set<Permission> permissions = permissionService.getUserPermissions(user);

        Set<GrantedAuthority> authorities = permissions.stream()
                .map(p -> new SimpleGrantedAuthority(p.getName()))
                .collect(Collectors.toSet());

//        RoleApp role = user.getRole();
//        SimpleGrantedAuthority roleAuthority = new SimpleGrantedAuthority(role.getName());

        if(user.getRole() != null){
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
        }


        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .accountLocked(user.getLocked())
                .disabled(!user.getEnabled())
                .build();
    }
}
