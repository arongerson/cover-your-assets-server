package com.ron.cover_your_assets.controller;

import java.util.Random;

public class CodeFactory {
	
	private static Random random = new Random();
	private static char[] codeChars = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
	
	private CodeFactory() {}
	
	public static String generateCode(int count) {
		StringBuilder code = new StringBuilder(count);
		for (int i = 0; i < count; i++) {
			code.append(codeChars[random.nextInt(codeChars.length)]);
		}
		return code.toString();
	}
}
