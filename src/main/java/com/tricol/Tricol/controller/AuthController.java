package com.tricol.Tricol.controller;

import com.tricol.Tricol.dto.request.LoginRequest;
import com.tricol.Tricol.dto.request.RegisterRequest;
import com.tricol.Tricol.dto.response.AuthResponse;
import com.tricol.Tricol.dto.response.ErrorResponse;
import com.tricol.Tricol.enums.AuditAction;
import com.tricol.Tricol.enums.AuditResourceType;
import com.tricol.Tricol.enums.AuditResult;
import com.tricol.Tricol.model.UserApp;
import com.tricol.Tricol.repository.UserRepository;
import com.tricol.Tricol.service.AuditService;
import com.tricol.Tricol.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    @Transactional
    public ResponseEntity<Object> register(@RequestBody RegisterRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                ErrorResponse error = ErrorResponse.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .error("Conflict")
                        .message("Email already exists")
                        .timestamp(LocalDateTime.now())
                        .build();
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
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

        } catch (Exception ex) {
            ErrorResponse error = ErrorResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .error("Internal Server Error")
                    .message("Failed to register user: " + ex.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<Object> login(@RequestBody LoginRequest request) {
        try {
            // Create the auth request object
            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

            Authentication authentication = authenticationManager.authenticate(authRequest);

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            String accessToken = jwtUtil.generateAccessToken(request.getEmail(), authorities);
            String refreshToken = jwtUtil.generateRefreshToken(request.getEmail());

            UserApp user = userRepository.findByEmail(authentication.getName()).orElseThrow();

            auditService.logWithUser(user, AuditAction.LOGIN_SUCCESS, AuditResourceType.AUTHENTICATION, user.getId(), AuditResult.SUCCESS);
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            AuthResponse response = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .build();

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException ex) {
            // Log failed login attempt
            auditService.logFailure(AuditAction.LOGIN_FAILURE, AuditResourceType.AUTHENTICATION, null);

            ErrorResponse error = ErrorResponse.builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .error("Unauthorized")
                    .message("Invalid email or password")
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);

        } catch (AuthenticationException ex) {
            // Other authentication errors (locked account, disabled, etc.)
            auditService.logFailure(AuditAction.LOGIN_FAILURE, AuditResourceType.AUTHENTICATION, null);

            ErrorResponse error = ErrorResponse.builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .error("Unauthorized")
                    .message("Authentication failed: " + ex.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);

        } catch (Exception ex) {
            ErrorResponse error = ErrorResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .error("Internal Server Error")
                    .message("Login failed: " + ex.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}