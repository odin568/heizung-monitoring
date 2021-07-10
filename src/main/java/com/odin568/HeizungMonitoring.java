package com.odin568;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class HeizungMonitoring {

	public static void main(String[] args) {
		SpringApplication.run(HeizungMonitoring.class, args);
	}

}
