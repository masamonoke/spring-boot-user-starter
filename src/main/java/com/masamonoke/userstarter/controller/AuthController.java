package com.masamonoke.userstarter.controller;

import com.masamonoke.userstarter.dto.AuthRequest;
import com.masamonoke.userstarter.dto.AuthResponse;
import com.masamonoke.userstarter.dto.RegisterRequest;
import com.masamonoke.userstarter.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/authenticate")
    ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh")
    ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return ResponseEntity.ok(authService.refreshToken(request, response));
    }
}
