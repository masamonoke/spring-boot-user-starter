package com.masamonoke.userstarter.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.stereotype.Component;

@Component
public class FileUtils {
	public String readFile(String pathToFile) throws IOException {
		var classLoader = getClass().getClassLoader();
		var inputStream = classLoader.getResourceAsStream(pathToFile);
		return readFromInputStream(inputStream);
	}

	private String readFromInputStream(InputStream inputStream) throws IOException {
		StringBuilder resultStringBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
		String line;
		while ((line = br.readLine()) != null) {
			resultStringBuilder.append(line).append("\n");
		}
		}
		return resultStringBuilder.toString();
	}
}
