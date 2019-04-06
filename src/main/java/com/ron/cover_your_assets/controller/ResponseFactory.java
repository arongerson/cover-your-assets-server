package com.ron.cover_your_assets.controller;

public class ResponseFactory {
	
	private static final String DELIMITER = ":;";
	private static final String NEW_LINE = "\n";
	private ResponseFactory() {}
	
	public static String generateRequestString(ResponseCode code, String json, String... tokens) {
		StringBuffer requestString = new StringBuffer();
		requestString.append(code.getNumber());
		requestString.append(DELIMITER);
		for (String token : tokens) {
			requestString.append(token).append(DELIMITER);
		}
		updateResponseWithJSON(json, requestString);
		// append new line
		requestString.append(NEW_LINE);
		return requestString.toString();
	}

	private static void updateResponseWithJSON(String json, StringBuffer requestString) {
		if (json != null) {
			requestString.append(json);
		} else {
			// remove the last delimiter, last two chars
			int lastIndex = requestString.length();
			requestString.delete(lastIndex - 2, lastIndex);
		}
	}
}
