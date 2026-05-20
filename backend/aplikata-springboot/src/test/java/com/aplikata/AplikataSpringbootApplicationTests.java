package com.aplikata;

import java.util.Calendar;

import org.junit.jupiter.api.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

class AplikataSpringbootApplicationTests {
	String token;

	@Test
	void contextLoads() {
		Calendar instance = Calendar.getInstance();
		instance.add(Calendar.SECOND, 50);

		token = JWT.create().withClaim("userId", 1).withClaim("username", "admin").withExpiresAt(instance.getTime())
				.sign(Algorithm.HMAC256("abcd"));

		System.out.println(token);
	}

	@Test
	void test() {
		JWTVerifier verifier = JWT.require(Algorithm.HMAC256("abcd")).build();
		DecodedJWT decodedJWT = verifier.verify("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3NzAzODcwNTgsInVzZXJJZCI6MSwidXNlcm5hbWUiOiJhZG1pbiJ9.EHjWf8-csRd_kZK1AJlIn66818vmWGwStF-1SeG3wFQ");
		System.out.println(decodedJWT.getClaim("userId").asInt());
		System.out.println(decodedJWT.getClaim("username").asString());

	}

}
