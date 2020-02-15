package com.jack.spring.gateway.serviceuno;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ServiceUnoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceUnoApplication.class, args);
	}
}
