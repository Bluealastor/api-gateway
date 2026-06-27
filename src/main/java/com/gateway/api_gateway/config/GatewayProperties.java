package com.gateway.api_gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

// @ConfigurationProperties è il modo corretto in Spring Boot per iniettare
// strutture complesse (liste, mappe, oggetti annidati) dal file YAML/properties.
//
// Differenza rispetto a @Value:
//   @Value("${gateway.public-paths}")  → funziona solo per scalari (String, int, ecc.)
//   @ConfigurationProperties(prefix = "gateway") → funziona per liste, mappe, oggetti
//
// Spring legge il blocco "gateway:" dal YAML e popola automaticamente i campi
// di questa classe. Il campo "publicPaths" si mappa su "public-paths" nel YAML
// grazie al relaxed binding di Spring Boot (kebab-case → camelCase).
// CLASSE DISABILITATA — sostituita da PublicPathsProperties.java
// @Component rimosso per evitare conflitto con il bean "gatewayProperties"
// registrato internamente da Spring Cloud Gateway (GatewayAutoConfiguration).
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {

    // Mappa su "gateway.public-paths" in application.yaml
    private List<String> publicPaths = List.of();

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }
}
