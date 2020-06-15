package com.ecommerce.shoppingportal;

import com.ecommerce.shoppingportal.config.RibbonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;

@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
@RibbonClient(name = "ACCOUNT-SERVICE", configuration = RibbonConfig.class)
public class ShoppingPortalApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShoppingPortalApplication.class, args);
	}

}
