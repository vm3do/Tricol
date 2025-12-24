package com.tricol.Tricol.controller;

import com.tricol.Tricol.dto.request.LoginRequest;
import com.tricol.Tricol.dto.request.RegisterRequest;
import com.tricol.Tricol.dto.response.AuthResponse;
import com.tricol.Tricol.enums.AuditAction;
import com.tricol.Tricol.enums.AuditResourceType;
import com.tricol.Tricol.enums.AuditResult;
import com.tricol.Tricol.model.UserApp;
import com.tricol.Tricol.repository.UserRepository;
import com.tricol.Tricol.service.AuditService;
import com.tricol.Tricol.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        UserApp user = UserApp.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .enabled(true)
                .locked(false)
                .build();

        user = userRepository.save(user);
        
        auditService.logSuccess(AuditAction.USER_CREATED, AuditResourceType.USER, user.getId());

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        //create the auth request object
            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

            Authentication authentication = authenticationManager.authenticate(authRequest);

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            String accessToken = jwtUtil.generateAccessToken(request.getEmail(), authorities);
            String refreshToken = jwtUtil.generateRefreshToken(request.getEmail());

            UserApp user = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (user != null) {
                auditService.logWithUser(user, AuditAction.LOGIN_SUCCESS, AuditResourceType.AUTHENTICATION, user.getId(), AuditResult.SUCCESS);
            }

            AuthResponse response = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .build();

            return ResponseEntity.ok(response);
    }
}
