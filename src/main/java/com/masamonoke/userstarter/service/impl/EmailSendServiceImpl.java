package com.masamonoke.userstarter.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.masamonoke.userstarter.service.EmailSendService;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class EmailSendServiceImpl implements EmailSendService {
	private final JavaMailSender javaMailSender;
	@Value("${mail.from}")
	private final String from;

	@Async
	@Override
	public void send(String to, String email, String subject) {
		try {
			var mimeMessage = javaMailSender.createMimeMessage();
			var mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
			mimeMessageHelper.setText(email, true);
			mimeMessageHelper.setTo(to);
			mimeMessageHelper.setSubject(subject);
			mimeMessageHelper.setFrom(from);
			javaMailSender.send(mimeMessage);
		} catch (MessagingException e) {
			log.error("Failed to send email", e);
		}
	}
}
