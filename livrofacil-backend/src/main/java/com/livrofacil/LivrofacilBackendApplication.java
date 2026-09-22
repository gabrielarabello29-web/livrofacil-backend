package com.livrofacil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LivrofacilBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(LivrofacilBackendApplication.class, args);
	}

}
