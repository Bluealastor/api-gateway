package com.gateway.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication è sufficiente per attivare Spring Cloud Gateway.
// Il discovery client Eureka si registra automaticamente grazie alla presenza
// di spring-cloud-starter-netflix-eureka-client nel pom.xml, senza annotazioni aggiuntive.
//
// IMPORTANTE: questo modulo usa Spring WebFlux (I/O reattivo non-bloccante),
// NON Spring MVC. Questo è il comportamento corretto per un API Gateway:
// gestisce molte connessioni simultanee con pochi thread.
// Per questo motivo NON va aggiunto spring-boot-starter-web al pom.xml —
// i due stack sono incompatibili sullo stesso progetto.
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
