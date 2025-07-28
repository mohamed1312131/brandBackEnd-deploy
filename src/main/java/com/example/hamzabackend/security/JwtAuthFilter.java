package com.example.hamzabackend.security;

import com.example.hamzabackend.entity.Admin;
import com.example.hamzabackend.repository.AdminRepository;
import com.example.hamzabackend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AdminRepository adminRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        System.out.println("🔍 Processing request: " + path); // Debug log

        String token = null;
        String email = null;

        // ✅ Read JWT token from "token" cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    System.out.println("🍪 Found token cookie"); // Debug log
                    break;
                }
            }
        }

        if (token != null) {
            try {
                email = jwtService.extractEmail(token);
                System.out.println("📧 Extracted email: " + email); // Debug log
            } catch (Exception e) {
                System.out.println("❌ Token extraction failed: " + e.getMessage()); // Debug log
            }
        } else {
            System.out.println("🚫 No token found - continuing without auth"); // Debug log
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Admin admin = adminRepository.findByEmail(email).orElse(null);
                if (admin != null && jwtService.isTokenValid(token, admin)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // ✅ If token validation fails, continue without authentication
                // Let Spring Security's permitAll() handle public endpoints
            }
        }
        System.out.println("✅ Continuing to filter chain"); // Debug log


        filterChain.doFilter(request, response);
    }
}