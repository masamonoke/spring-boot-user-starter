package com.masamonoke.userstarter.dto;

public record AuthRequest(
        String username,
        String password
) {}
