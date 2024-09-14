package com.masamonoke.userstarter.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UserNotFoundException extends Exception {
	public UserNotFoundException(String msg) {
		super(msg);
	}
}
