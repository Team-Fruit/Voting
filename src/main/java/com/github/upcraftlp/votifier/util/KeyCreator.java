package com.github.upcraftlp.votifier.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

public class KeyCreator {
	public static Key createKeyFrom(String token) {
		return new SecretKeySpec(token.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}
}