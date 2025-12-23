package com.HeatTrackingWeb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
public class Application {

	public static void main(String[] args) {
        System.setProperty("java.net.preferIPv6Addresses", "true");
		SpringApplication.run(Application.class, args);
	}

}
