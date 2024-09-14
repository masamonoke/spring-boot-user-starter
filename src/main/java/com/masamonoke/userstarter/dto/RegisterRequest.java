package com.masamonoke.userstarter.dto;

public record RegisterRequest(
        String username,
        String email,
        String password
) {}
