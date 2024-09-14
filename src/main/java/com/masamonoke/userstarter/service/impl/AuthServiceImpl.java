package com.masamonoke.userstarter.service.impl;

import com.masamonoke.userstarter.dto.AuthRequest;
import com.masamonoke.userstarter.dto.AuthResponse;
import com.masamonoke.userstarter.dto.RegisterRequest;
import com.masamonoke.userstarter.model.AuthToken;
import com.masamonoke.userstarter.model.ConfirmationToken;
import com.masamonoke.userstarter.model.User;
import com.masamonoke.userstarter.repo.AuthTokenRepo;
import com.masamonoke.userstarter.repo.ConfirmationTokenRepo;
import com.masamonoke.userstarter.repo.UserRepo;
import com.masamonoke.userstarter.service.AuthService;
import com.masamonoke.userstarter.service.EmailConfirmService;
import com.masamonoke.userstarter.service.EmailSendService;
import com.masamonoke.userstarter.service.JwtService;
import com.masamonoke.userstarter.utils.FileUtils;
import com.masamonoke.userstarter.utils.TokenUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
	private final UserRepo userRepo;
    private final JwtService jwtService;
    private final AuthTokenRepo authTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
	private final EmailConfirmService emailConfirmationService;
	private final ConfirmationTokenRepo confirmationTokenRepo;
	private final EmailSendService emailSendService;
	@Value("${server.port}")
	private String port;
	@Value("${mail.confirm-link-pattern}")
	private String confirmLinkPattern;
	private final String emailConfirmPage;
	private final TokenUtils tokenUtils;

    public AuthServiceImpl(UserRepo userRepo, JwtService jwtService, AuthTokenRepo authTokenRepo, PasswordEncoder passwordEncoder,
						   AuthenticationManager authenticationManager, EmailConfirmService emailConfirmationService,
						   ConfirmationTokenRepo confirmationTokenRepo, EmailSendService emailSendService, TokenUtils tokenUtils,
						   @Value("${mail.confirmation-page}") String emailConfirmPagePath, FileUtils fileUtils) throws IOException
	{
        this.userRepo = userRepo;
        this.jwtService = jwtService;
        this.authTokenRepo = authTokenRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailConfirmationService = emailConfirmationService;
        this.confirmationTokenRepo = confirmationTokenRepo;
        this.emailSendService = emailSendService;
		this.tokenUtils = tokenUtils;
		emailConfirmPage = fileUtils.readFile(emailConfirmPagePath);
    }

    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request) {
        var user = User.builder()
            .username(request.username())
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .build();
        var savedUser = userRepo.save(user);
        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, accessToken);

		var uuid = UUID.randomUUID().toString();
		var confirmationToken = new ConfirmationToken(uuid, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), user);
		confirmationTokenRepo.save(confirmationToken);
		var link = String.format(confirmLinkPattern, port, uuid);
		emailSendService.send(request.email(), emailConfirmationService.buildEmail(request.username(), link, emailConfirmPage), "Confirm your email");

		return new AuthResponse(accessToken, refreshToken);
    }

    private void saveUserToken(User savedUser, String accessToken) {
        var t = AuthToken.builder()
                .user(savedUser)
                .token(accessToken)
                .isExpired(false)
                .isRevoked(false)
                .build();
        authTokenRepo.save(t);
    }

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        var authToken = new UsernamePasswordAuthenticationToken(request.username(), request.password());
        authenticationManager.authenticate(authToken); // makes slient exception if not valid user data passed and returns from function
        var user = userRepo.findByUsername(request.username()).orElseThrow();
        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        tokenUtils.revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authentication header is null or is in incorrect format");
        }

        var bearerStartIdx = 7;
        var refreshToken = authHeader.substring(bearerStartIdx);
        var username = jwtService.extractUsername(refreshToken);

        if (username != null) {
            var user = userRepo.findByUsername(username).orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                tokenUtils.revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                return new AuthResponse(accessToken, refreshToken);
            }

			throw new IllegalStateException("Cannot extract username from token");
        }

		throw new IllegalStateException("Null username in token: wrong token format");
    }

}
