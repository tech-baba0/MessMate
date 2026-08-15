package com.messmate.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MessMateApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessMateApplication.class, args);
	}

}
