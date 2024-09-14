package com.masamonoke.userstarter.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InvalidUserException extends Exception {
	public InvalidUserException(String msg) {
		super(msg);
	}
}
