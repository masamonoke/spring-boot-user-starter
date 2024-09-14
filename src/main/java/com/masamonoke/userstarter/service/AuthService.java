package com.masamonoke.userstarter.service;

import com.masamonoke.userstarter.dto.AuthRequest;
import com.masamonoke.userstarter.dto.AuthResponse;
import com.masamonoke.userstarter.dto.RegisterRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse authenticate(AuthRequest request);

    AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
