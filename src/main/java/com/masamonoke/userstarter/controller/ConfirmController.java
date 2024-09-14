package com.masamonoke.userstarter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.masamonoke.userstarter.exception.UserNotFoundException;
import com.masamonoke.userstarter.service.EmailConfirmService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/confirm")
@RequiredArgsConstructor
public class ConfirmController {
	private final EmailConfirmService emailConfirmService;

	@GetMapping
	public String confirmEmail(@RequestParam("token") String token) {
		return emailConfirmService.confirmToken(token);
	}

	@GetMapping("/restore")
	public String restoreAccount(@RequestParam("token") String token, @RequestParam("username") String username) throws UserNotFoundException {
		return emailConfirmService.restoreAccount(token, username);
	}
}
