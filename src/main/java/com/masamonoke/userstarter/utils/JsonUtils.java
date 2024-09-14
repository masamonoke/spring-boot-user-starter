package com.masamonoke.userstarter.utils;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
	@SuppressWarnings("unchecked")
	public static Map<String, String> decodeToken(String token) throws JsonProcessingException {
		var chunks = token.split("\\.");
		var decoder = Base64.getUrlDecoder();
		var payload = new String(decoder.decode(chunks[1]));
		var om = new ObjectMapper();
		return om.readValue(payload, HashMap.class);
	}

	public static String getTokenFromHeader(String authHeader) {
		var tokenStartIdx = 7;
		return authHeader.substring(tokenStartIdx);
	}
}
