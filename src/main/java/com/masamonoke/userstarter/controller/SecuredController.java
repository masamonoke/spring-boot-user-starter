package com.masamonoke.userstarter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/secured_test")
class SecuredController {
	@GetMapping
	ResponseEntity<String> test() {
		return ResponseEntity.ok("Hello from secured endpoint");
	}
}
