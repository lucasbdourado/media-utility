package com.lucasdourado.mediautility;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MediaUtilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediaUtilityApplication.class, args);
	}

}
