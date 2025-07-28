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
        System.out.println("🔍 Processing request: " + path);

        try {
            String token = null;
            String email = null;

            // ✅ Read JWT token from "token" cookie
            if (request.getCookies() != null) {
                System.out.println("🍪 Checking cookies...");
                for (Cookie cookie : request.getCookies()) {
                    if ("token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        System.out.println("🍪 Found token cookie");
                        break;
                    }
                }
            } else {
                System.out.println("🚫 No cookies found");
            }

            if (token != null) {
                try {
                    System.out.println("📧 Attempting to extract email from token...");
                    email = jwtService.extractEmail(token);
                    System.out.println("📧 Extracted email: " + email);
                } catch (Exception e) {
                    System.out.println("❌ Token extraction failed: " + e.getMessage());
                    e.printStackTrace(); // Print full stack trace for debugging
                }
            } else {
                System.out.println("🚫 No token found - continuing without auth");
            }

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    System.out.println("🔐 Attempting to authenticate user: " + email);
                    Admin admin = adminRepository.findByEmail(email).orElse(null);
                    if (admin != null && jwtService.isTokenValid(token, admin)) {
                        System.out.println("✅ Token is valid, setting authentication");
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        System.out.println("❌ Invalid token or admin not found");
                    }
                } catch (Exception e) {
                    System.out.println("❌ Authentication failed: " + e.getMessage());
                    e.printStackTrace(); // Print full stack trace for debugging
                }
            }

            System.out.println("✅ Continuing to filter chain for: " + path);
            filterChain.doFilter(request, response);
            System.out.println("✅ Filter chain completed for: " + path);

        } catch (Exception e) {
            System.out.println("💥 CRITICAL ERROR in JWT Filter: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to see where it's coming from
        }
    }
}