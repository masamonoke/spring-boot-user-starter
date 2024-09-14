package com.masamonoke.userstarter.utils;

import org.springframework.stereotype.Component;

import com.masamonoke.userstarter.model.User;
import com.masamonoke.userstarter.repo.AuthTokenRepo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenUtils {
	private final AuthTokenRepo authTokenRepo;

    public void revokeAllUserTokens(User user) {
        var validUserTokens = authTokenRepo.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            return;
        }
        validUserTokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        authTokenRepo.saveAll(validUserTokens);
    }
}
