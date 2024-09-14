package com.masamonoke.userstarter.service.impl;

import java.text.MessageFormat;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.masamonoke.userstarter.exception.UserNotFoundException;
import com.masamonoke.userstarter.repo.ConfirmationTokenRepo;
import com.masamonoke.userstarter.repo.UserRepo;
import com.masamonoke.userstarter.service.EmailConfirmService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailConfirmServiceImpl implements EmailConfirmService {
	private final UserRepo userRepo;
	private final ConfirmationTokenRepo confirmationTokenRepo;

	@Override
	public String confirmToken(String token) {
		var confirmationToken = confirmationTokenRepo
			.findByToken(token)
			.orElseThrow(() -> new IllegalArgumentException("Confirmation token not found"));

		if (confirmationToken.getConfirmedAt() != null) {
			throw new IllegalStateException("Email is already confirmed");
		}

		var expiredAt = confirmationToken.getExpiredAt();

		if (expiredAt.isBefore(LocalDateTime.now())) {
			throw new IllegalStateException("Token expired");
		}

		confirmationTokenRepo.updateConfirmedAt(token, LocalDateTime.now());
		userRepo.setEmailConfirmed(confirmationToken.getUser().getEmail());
		return "confirmed";
	}

	@Override
	public String buildEmail(String name, String link, String emailPage) {
		return MessageFormat.format(emailPage, name, link);
	}

	@Transactional
	@Override
	public String restoreAccount(String token, String username) throws UserNotFoundException {
		var confirmationToken = confirmationTokenRepo
			.findByToken(token)
			.orElseThrow(() -> new IllegalArgumentException("Confirmation token not found"));
		var user = userRepo
			.findByUsername(username)
			.orElseThrow(() -> new UserNotFoundException(String.format("Failed to find user with username %s", username)));

		var expiredAt = confirmationToken.getExpiredAt();

		if (expiredAt.isBefore(LocalDateTime.now())) {
			throw new IllegalStateException("Token expired");
		}

		user.setEnabled(true);
		userRepo.save(user);

		confirmationTokenRepo.updateConfirmedAt(token, LocalDateTime.now());

		return "restored";
	}
}
