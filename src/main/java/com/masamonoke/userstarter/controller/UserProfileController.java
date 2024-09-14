package com.masamonoke.userstarter.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.masamonoke.userstarter.exception.InvalidUserException;
import com.masamonoke.userstarter.exception.UserDataNotMatchException;
import com.masamonoke.userstarter.exception.UserEmailIsNotConfirmed;
import com.masamonoke.userstarter.exception.UserNotFoundException;
import com.masamonoke.userstarter.model.User;
import com.masamonoke.userstarter.service.UserProfileService;

import static com.masamonoke.userstarter.utils.JsonUtils.getTokenFromHeader;

@RestController
@RequestMapping("/api/v1/profile/user")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {
	private final UserProfileService service;

	@GetMapping()
	ResponseEntity<User> getUserById(@RequestParam("id") Long id, @RequestHeader("Authorization") String header) throws UserNotFoundException, InvalidUserException, JsonProcessingException {
		var token = getTokenFromHeader(header);
		return ResponseEntity.ok(service.getUserById(id, token));
	}

	@PutMapping("/username")
	ResponseEntity<User> updateUsername(@RequestParam("username") String username, @RequestParam("id") Long id, @RequestHeader("Authorization") String header)
		throws InvalidUserException, UserNotFoundException, JsonProcessingException
	{
		var token = getTokenFromHeader(header);
		return ResponseEntity.ok(service.updateUsername(id, username, token));
	}

	@PutMapping("/password")
	ResponseEntity<User> updatePassword(@RequestBody User user, @RequestHeader("Authorization") String header)
		throws UserNotFoundException, InvalidUserException, JsonProcessingException
	{
		var token = getTokenFromHeader(header);
		return ResponseEntity.ok(service.updatePassword(user, token));
	}

	@DeleteMapping
	void deleteAccount(@RequestParam("id") Long id, @RequestHeader("Authorization") String header)
		throws JsonProcessingException, UserNotFoundException, InvalidUserException, UserEmailIsNotConfirmed
	{
		var token = getTokenFromHeader(header);
		service.deleteAccount(id, token);
	}

	@PutMapping("/restore")
	void restoreAccount(@RequestBody User user) throws UserNotFoundException, UserDataNotMatchException {
		service.restoreAccount(user);
	}

	@PutMapping("/email")
	ResponseEntity<User> updateEmail(@RequestParam("email") String email, @RequestParam("id") Long id, @RequestHeader("Authorization") String header)
		throws JsonProcessingException, UserNotFoundException, InvalidUserException, UserEmailIsNotConfirmed
	{
		var token = getTokenFromHeader(header);
		return ResponseEntity.ok(service.updateEmail(id, email, token));
	}


	// when user have token that doesn't belong to requested user or
	// try to get data that he doesn't have permission to
	@ExceptionHandler(InvalidUserException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public void invalidUserExceptionHandler(InvalidUserException e) {
		log.error(e.getMessage());
	}

	// when we have no token and comparing input and authorize by username and password that can be not match
	@ExceptionHandler(UserDataNotMatchException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public void userDataNotMatchExceptionHandler(UserDataNotMatchException e) {
		log.error(e.getMessage());
	}

	@ExceptionHandler(UserEmailIsNotConfirmed.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public void userIsNotEnabledHandler(UserEmailIsNotConfirmed e) {
		log.error(e.getMessage());
	}
}
