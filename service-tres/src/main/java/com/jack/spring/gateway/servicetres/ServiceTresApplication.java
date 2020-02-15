package com.jack.spring.gateway.servicetres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ServiceTresApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceTresApplication.class, args);
	}
}
