package com.pink.pfa;

import org.springframework.boot.SpringApplication;

public class TestPfaApplication {

	public static void main(String[] args) {
		SpringApplication.from(PfaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
