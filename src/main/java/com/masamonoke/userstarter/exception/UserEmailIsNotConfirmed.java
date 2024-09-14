package com.masamonoke.userstarter.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UserEmailIsNotConfirmed extends Exception {
	public UserEmailIsNotConfirmed(String msg) {
		super(msg);
	}
}

