package com.m2miage;

//import com.m2miage.boundary.DemandeChannel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableCircuitBreaker
//@EnableBinding(DemandeChannel.class)
public class ServiceDemandeApplication {
	public static void main(String[] args) {
		SpringApplication.run(ServiceDemandeApplication.class, args);
	}
}
