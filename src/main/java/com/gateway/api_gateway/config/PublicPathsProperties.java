package com.gateway.api_gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

// Legge "gateway.public-paths" dal YAML tramite @ConfigurationProperties.
// Il nome della classe è volutamente diverso da "GatewayProperties" perché
// Spring Cloud Gateway registra già internamente un bean con quel nome —
// avere due bean con lo stesso nome causa BeanDefinitionOverrideException.
@Component
@ConfigurationProperties(prefix = "gateway")
public class PublicPathsProperties {

    // Mappa su "gateway.public-paths" in application.yaml
    private List<String> publicPaths = List.of();

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }
}
