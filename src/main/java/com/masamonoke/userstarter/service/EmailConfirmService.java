package com.masamonoke.userstarter.service;

import com.masamonoke.userstarter.exception.UserNotFoundException;

public interface EmailConfirmService {
	String confirmToken(String token);

	String buildEmail(String name, String link, String emailPage);

	String restoreAccount(String token, String username) throws UserNotFoundException;
}
