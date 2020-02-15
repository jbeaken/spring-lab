package com.jack.spring.gateway.servicedos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ServiceDosApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceDosApplication.class, args);
	}
}
 