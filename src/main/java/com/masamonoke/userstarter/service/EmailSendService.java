package com.masamonoke.userstarter.service;

public interface EmailSendService {
	void send(String to, String email, String subject);
}
