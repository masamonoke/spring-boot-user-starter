package com.masamonoke.userstarter.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UserDataNotMatchException extends Exception {
	public UserDataNotMatchException(String msg) {
		super(msg);
	}
}
