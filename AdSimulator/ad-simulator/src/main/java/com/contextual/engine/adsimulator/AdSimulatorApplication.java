package com.contextual.engine.adsimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AdSimulatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdSimulatorApplication.class, args);
	}

}
