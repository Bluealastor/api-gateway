package com.gateway.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

// Configura CORS per tutti gli endpoint del Gateway.
//
// Necessario perché il viewer 360° usa un WebView che carica HTML da una
// stringa locale (origine "null") e poi richiede il video dal NAS tramite HTTP.
// Senza questi header, il browser integrato nel WebView blocca la richiesta
// con un errore CORS "No 'Access-Control-Allow-Origin' header".
//
// Usiamo CorsWebFilter (stack reattivo WebFlux) anziché @CrossOrigin o
// WebMvcConfigurer, che appartengono allo stack Servlet — incompatibile con Gateway.
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Permette richieste da qualsiasi origine (inclusa "null" per data:/blob: URL)
        config.addAllowedOriginPattern("*");

        // Metodi HTTP necessari per lo streaming video e le API REST
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        // Header standard + Authorization per JWT
        config.addAllowedHeader("*");

        // Espone l'header Range al client (necessario per il seek video)
        config.addExposedHeader("Content-Range");
        config.addExposedHeader("Accept-Ranges");
        config.addExposedHeader("Content-Length");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
