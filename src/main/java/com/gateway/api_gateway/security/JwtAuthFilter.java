package com.gateway.api_gateway.security;

import com.gateway.api_gateway.config.PublicPathsProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// GlobalFilter: si applica automaticamente a TUTTE le rotte del Gateway,
// senza doverlo dichiarare rotta per rotta in application.yaml.
//
// DIFFERENZA RISPETTO AD AUTH-SERVICE:
// In auth-service avevamo OncePerRequestFilter (stack Servlet/bloccante).
// Qui usiamo GlobalFilter con WebFlux (stack reattivo/non-bloccante).
// La logica è la stessa, ma l'API è diversa:
//   - niente HttpServletRequest/Response
//   - tutto passa attraverso ServerWebExchange
//   - il "prossimo filtro" si chiama con chain.filter(exchange) che ritorna un Mono<Void>
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    // GatewayProperties legge "gateway.public-paths" dal YAML tramite
    // @ConfigurationProperties — supporta liste, a differenza di @Value.
    private final PublicPathsProperties publicPathsProperties;

    public JwtAuthFilter(JwtUtil jwtUtil, PublicPathsProperties publicPathsProperties) {
        this.jwtUtil = jwtUtil;
        this.publicPathsProperties = publicPathsProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Se il path è tra quelli pubblici, lascia passare senza controlli
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Legge l'header Authorization dalla richiesta
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Se l'header manca o non inizia con "Bearer ", risponde 401
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        // Estrae il token grezzo rimuovendo il prefisso "Bearer "
        String token = authHeader.substring(7);

        // Valida la firma e la scadenza del token
        if (!jwtUtil.isTokenValid(token)) {
            return unauthorized(exchange);
        }

        // Token valido: aggiunge lo username come header interno prima di inoltrare.
        // I microservizi downstream possono leggere X-Auth-User per sapere chi sta chiamando,
        // senza dover rileggere e validare il JWT da zero.
        String username = jwtUtil.extractUsername(token);
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.header("X-Auth-User", username))
                .build();

        return chain.filter(mutatedExchange);
    }

    // Questo filtro deve girare per primo, prima dei filtri interni del Gateway.
    // Ordered.HIGHEST_PRECEDENCE garantisce la priorità massima.
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isPublicPath(String path) {
        return publicPathsProperties.getPublicPaths().contains(path);
    }

    // Termina la richiesta con 401 Unauthorized senza inoltrarla al servizio downstream.
    // exchange.getResponse().setComplete() chiude lo stream reattivo — equivale a
    // "non scrivere nulla nel body e chiudi la connessione".
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
