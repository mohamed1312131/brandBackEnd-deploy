package com.example.hamzabackend.controller;


import com.example.hamzabackend.DTO.auth.AuthResponse;
import com.example.hamzabackend.DTO.auth.LoginRequest;
import com.example.hamzabackend.DTO.auth.RegisterRequest;
import com.example.hamzabackend.entity.Admin;
import com.example.hamzabackend.repository.AdminRepository;
import com.example.hamzabackend.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (adminRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        Admin admin = Admin.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        adminRepository.save(admin);
        String token = jwtService.generateToken(admin);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        Admin admin = adminRepository.findByEmail(request.getEmail()).orElse(null);

        if (admin == null || !passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        String token = jwtService.generateToken(admin);

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        // ✅ RETURN THE USER DATA ON SUCCESS
        Map<String, String> userDetails = Map.of(
                "email", admin.getEmail(),
                "username", admin.getUsername()
        );

        return ResponseEntity.ok(userDetails);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(false) // NOTE: for localhost, this should be false unless using HTTPS
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok("Logged out");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentAdmin(@AuthenticationPrincipal Admin admin) {
        // If the request reaches this point, the JWT was valid and the user
        // was loaded by your JwtAuthFilter. Spring injects it directly.
        if (admin != null) {
            return ResponseEntity.ok(Map.of(
                    "email", admin.getEmail(),
                    "username", admin.getUsername()
            ));
        }
        // This case is unlikely if your filter is correct, but good for safety.
        return ResponseEntity.status(401).body("Unauthorized");
    }



}
