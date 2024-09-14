package com.masamonoke.userstarter.service.impl;

import com.masamonoke.userstarter.utils.FileUtils;
import com.masamonoke.userstarter.utils.TokenUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.masamonoke.userstarter.exception.InvalidUserException;
import com.masamonoke.userstarter.exception.UserDataNotMatchException;
import com.masamonoke.userstarter.exception.UserEmailIsNotConfirmed;
import com.masamonoke.userstarter.exception.UserNotFoundException;
import com.masamonoke.userstarter.model.ConfirmationToken;
import com.masamonoke.userstarter.model.Role;
import com.masamonoke.userstarter.model.User;
import com.masamonoke.userstarter.repo.ConfirmationTokenRepo;
import com.masamonoke.userstarter.repo.UserRepo;
import com.masamonoke.userstarter.service.EmailConfirmService;
import com.masamonoke.userstarter.service.EmailSendService;
import com.masamonoke.userstarter.service.UserProfileService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import static com.masamonoke.userstarter.utils.JsonUtils.decodeToken;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {
	private final UserRepo userRepo;
	private final PasswordEncoder passwordEncoder;
	private final EmailSendService emailSendService;
	private final EmailConfirmService emailConfirmService;
	private final ConfirmationTokenRepo confirmationTokenRepo;
	@Value("${server.port}")
	private String port;
	@Value("${mail.confirm-link-pattern}")
	private String confirmLinkPattern;
	@Value("${mail.restore-link-pattern}")
	private String restoreLinkPattern;
	private final String emailConfirmationPage;
	private final String emailRestoreUserPage;
	private final TokenUtils tokenUtils;

	public UserProfileServiceImpl(UserRepo userRepo, PasswordEncoder passwordEncoder, EmailSendService emailSendService, EmailConfirmService emailConfirmService,
								  ConfirmationTokenRepo confirmationTokenRepo, @Value("${mail.restoration-page}") String emailRestoreUserPagePath, TokenUtils tokenUtils,
								  @Value("${mail.confirmation-page}") String emailConfirmationPagePath, FileUtils fileUtils) throws IOException
	{
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
		this.emailSendService = emailSendService;
		this.emailConfirmService = emailConfirmService;
		this.confirmationTokenRepo = confirmationTokenRepo;
		this.tokenUtils = tokenUtils;

		emailConfirmationPage = fileUtils.readFile(emailConfirmationPagePath);
		emailRestoreUserPage = fileUtils.readFile(emailRestoreUserPagePath);
	}

	@Override
    public User getUserById(Long id, String token) throws JsonProcessingException, UserNotFoundException, InvalidUserException {
		var user = userRepo.findById(id).orElse(null);
		if (user != null && isValidUser(token, user)) {
			return user;
		}

		if (user == null) {
			throw new UserNotFoundException(String.format("User with id %d not found", id));
		} else {
			throw new InvalidUserException(String.format("User id=%d is not valid", id));
		}
    }

	private boolean isValidUser(String token, User user) throws JsonProcessingException {
		var map = decodeToken(token);
		var username = map.get("sub");
		boolean isTokenBelongsToUser = username.equals(user.getUsername());
		boolean isAdmin = Role.ADMIN.equals(user.getRole());

		return isTokenBelongsToUser || isAdmin;
	}

    @Override
	@Transactional
    public User updateUsername(Long id, String username, String token) throws JsonProcessingException, UserNotFoundException, InvalidUserException {
		var user = userRepo.findById(id)
			.orElseThrow(() -> new UserNotFoundException(String.format("User with id=%s not found", id)));

		if (isValidUser(token, user)) {
			user.setUsername(username);
			return userRepo.save(user);
		}

		throw new InvalidUserException(String.format("User username=%s is not valid", username));
    }

    @Override
    public User updatePassword(User user, String token) throws JsonProcessingException, UserNotFoundException, InvalidUserException {
		var savedUser = userRepo.findById(user.getId())
			.orElseThrow(() -> new UserNotFoundException(String.format("User with id=%d not found", user.getId())));

		if (!isValidUser(token, savedUser)) {
			throw new InvalidUserException(String.format("User username=%s is not valid", user.getUsername()));
		}

		savedUser.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(savedUser);
    }

    @Override
    public User updateEmail(Long id, String email, String token) throws JsonProcessingException, InvalidUserException, UserNotFoundException, UserEmailIsNotConfirmed {
		var user = userRepo
			.findById(id)
			.orElseThrow(() -> new UserNotFoundException(String.format("User with id=%d not found", id)));

		if (!isValidUser(token, user)) {
			throw new InvalidUserException(String.format("User id=%d is not valid", user.getId()));
		}

		if (!user.isEmailConfirmed()) {
			throw new UserEmailIsNotConfirmed(String.format("User id=%d didn't confirm their email.", user.getId()));
		}

		user.setEmailConfirmed(false);
		user.setEmail(email);

		var uuid = UUID.randomUUID().toString();
		var confirmationToken = new ConfirmationToken(uuid, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), user);
		confirmationTokenRepo.save(confirmationToken);
		var link = String.format(confirmLinkPattern, port, uuid);
		emailSendService.send(email, emailConfirmService.buildEmail(user.getUsername(), link, emailConfirmationPage), "New Email Confirmation");
		return user;
    }

    @Override
    public void deleteAccount(Long id, String token) throws JsonProcessingException, InvalidUserException, UserNotFoundException, UserEmailIsNotConfirmed {
		var user = userRepo.findById(id)
			.orElseThrow(() -> new UserNotFoundException(String.format("User with id=%d nof found", id)));

		if (!isValidUser(token, user)) {
			throw new InvalidUserException(String.format("User id=%d is not valid", user.getId()));
		}

		if (!user.isEmailConfirmed()) {
			throw new UserEmailIsNotConfirmed(String.format("User id=%d didn't confirm their email.", user.getId()));
		}

		user.setEnabled(false);
		tokenUtils.revokeAllUserTokens(user);
		userRepo.save(user);
    }

    @Override
    public void restoreAccount(User user) throws UserNotFoundException, UserDataNotMatchException {
		var savedUser = userRepo.findByUsername(user.getUsername())
			.orElseThrow(() -> new UserNotFoundException(String.format("User with username=%s not found", user.getUsername())));

		var isUsernameEq = savedUser.getUsername().equals(user.getUsername());
		var isEmailEq = savedUser.getEmail().equals(user.getEmail());
		var isPasswordHashEq = passwordEncoder.matches(user.getPassword(), savedUser.getPassword());

		if (isEmailEq && isUsernameEq && isPasswordHashEq) {
			var uuid = UUID.randomUUID().toString();
			var confirmationToken = new ConfirmationToken(uuid, LocalDateTime.now(), LocalDateTime.now().plusMinutes(15), savedUser);
			confirmationTokenRepo.save(confirmationToken);
			var link = String.format(restoreLinkPattern, port, uuid, savedUser.getUsername());
			emailSendService.send(user.getEmail(), emailConfirmService.buildEmail(user.getUsername(), link, emailRestoreUserPage), "Account Restore");
		} else {
			throw new UserDataNotMatchException(String.format("There is no user with username=%s or email=%s or wrong password passed", user.getUsername(), user.getEmail()));
		}
    }
}
