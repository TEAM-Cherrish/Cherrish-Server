package com.sopt.cherrish.global.swagger;

import io.swagger.v3.oas.models.examples.Example;

public record ExampleHolder(
	Example holder,
	String name,
	int code
) {
	public static ExampleHolder of(Example holder, String name, int code) {
		return new ExampleHolder(holder, name, code);
	}
}
