package org.elsevier.poc.httpbin.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Ryan Baxter
 */
@Service
public class HttpBinService {

    private WebClient rest;

    public HttpBinService(WebClient.Builder builder) {
        this.rest = builder.baseUrl("https://httpbin.org").build();
    }

    public Mono<Map> get() {
        return rest.get().uri("/get").retrieve().bodyToMono(Map.class);
    }

    public Mono<Map> delay(int seconds) {
        return rest.get().uri("/delay/{seconds}", seconds).retrieve().bodyToMono(Map.class);
    }

    public Supplier<Mono<Map>> delaySupplier(int seconds) {
        return () -> this.delay(seconds);
    }

    public Flux<String> fluxDelay(int seconds) {
        return Flux.just("1", "2", "3").delayElements(Duration.ofSeconds(seconds));
    }
}
