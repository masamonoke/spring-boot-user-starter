package com.masamonoke.userstarter.controller.exception;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.masamonoke.userstarter.exception.UserNotFoundException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler {
	@ExceptionHandler(JsonProcessingException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public void jsonProcessingExceptionHandler(JsonProcessingException e) {
		log.error(e.getMessage());
	}

	@ExceptionHandler(UserNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public void userNotFoundExceptionHandler(UserNotFoundException e) {
		log.error(e.getMessage());
	}
}
