package com.jack.spring.gateway.cloudgateway;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.chaos.monkey.shopping.domain.Product;

//import org.springframework.cloud.security.oauth2.gateway.TokenRelayGatewayFilterFactory;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;

@SpringBootApplication
@RestController
@EnableDiscoveryClient
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

//	@Autowired
//	private TokenRelayGatewayFilterFactory filterFactory;

	@Bean
	public RouteLocator routes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(p -> p.path("/hotdeals/**").filters(f ->
						f.circuitBreaker(c -> c.setName("hotdeals").setFallbackUri("forward:/fallback"))).uri("lb://hot-deals"))
				.route(p -> p.path("/fashion/**")
						.filters(f -> f.circuitBreaker(c -> c.setName("fashion").setFallbackUri("forward:/fallback")))
						.uri("lb://fashion-bestseller"))
				.route(p -> p.path("/httpbin/**")
						.filters(f -> f.circuitBreaker(c -> c.setName("myCircuitBreaker").setFallbackUri("forward:/fallback"))).uri("lb://httpbin"))
				.route(p -> p.path("/toys/**")
						.filters(f -> f.requestRateLimiter().configure(c -> c.setRateLimiter(redisRateLimiter()).setKeyResolver(allOneWorldKeyResolver()))).uri("lb://toys-bestseller"))
				.build();
	}

	@GetMapping("/fallback")
	public ResponseEntity<List<Product>> fallback() {
		System.out.println("fallback enabled");
		HttpHeaders headers = new HttpHeaders();
		headers.add("fallback", "true");
		return ResponseEntity.ok().headers(headers).body(Collections.emptyList());
	}

	@Bean
	@Primary
	KeyResolver allOneWorldKeyResolver() {
		return exchange -> Mono.just("1");
	}

	//    @Bean
	KeyResolver queryQueryParamResolver() {
		return exchange -> Mono.just(exchange.getRequest().getQueryParams().getFirst("user"));
	}

	@Bean
	KeyResolver sessionResolver() {
		KeyResolver sessionKeyResolver = exchange -> Mono.just( exchange.getSession().map(s -> s.getAttribute("JSESSIONID") ).toString() );
		return sessionKeyResolver;
	}

//	@Bean
//	public MapReactiveUserDetailsService reactiveUserDetailsService() {
//		UserDetails user = User.withDefaultPasswordEncoder().username("user").password("password").roles("USER").build();
//		return new MapReactiveUserDetailsService(user);
//	}

	@Bean
	RedisRateLimiter redisRateLimiter() {
		return new RedisRateLimiter(1, 1);
	}

	@Bean
	public WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}

//    @Bean
//    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
//        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
//                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
//                .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(4)).build()).build());
//    }

	@Bean
	public ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory() {
		ReactiveResilience4JCircuitBreakerFactory factory = new ReactiveResilience4JCircuitBreakerFactory();

		factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
				.circuitBreakerConfig(
						CircuitBreakerConfig.custom()
								.failureRateThreshold(50)
								.waitDurationInOpenState(ofMillis(10000))
								.slidingWindow(20, 20, COUNT_BASED)
								.build())
				.timeLimiterConfig(
						TimeLimiterConfig.custom()
								.timeoutDuration(ofSeconds(2))
								.build())
				.build());

		factory.configure(configBuilder -> configBuilder
						.circuitBreakerConfig(
								CircuitBreakerConfig.custom()
										.failureRateThreshold(33)
										.waitDurationInOpenState(ofMillis(5000))
										.slidingWindow(10, 10, COUNT_BASED)
										.build())
						.timeLimiterConfig(
								TimeLimiterConfig.custom()
										.timeoutDuration(ofSeconds(2))
										.build())
						.build(),
				"myCircuitBreaker");

		return factory;
	}

//    @Bean
//    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
//        CircuitBreakerRegistry cbr = CircuitBreakerRegistry.ofDefaults();
//
//        return factory -> {
//            factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
//                    .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
//                    .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(1)).build())
//                    .circuitBreakerConfig(CircuitBreakerConfig.custom().failureRateThreshold(1)
//                            .slowCallRateThreshold(5).slowCallRateThreshold(2).build()).build());
//        };
//    }

//    @Bean
//    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
//        CircuitBreakerRegistry cbr = CircuitBreakerRegistry.ofDefaults();
//
//        return factory -> {
//            factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
//                    .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
//                    .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(1)).build()).build());
//        };
//    }


//    @Bean
//    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
//        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
//                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
//                .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(40)).build()).build());
//    }

//    @Bean
//    public Customizer<ReactiveResilience4JCircuitBreakerFactory> fastCustomizer() {
//        return factory -> factory.configure((builder -> builder.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
//                .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(40)).build())), "fastCustomizer");
//    }


//    @Bean
//    public Customizer<ReactiveResilience4JCircuitBreakerFactory> helloworldCircuitBreaker(){
//        return factory -> {
//            factory.configure(builder -> builder.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults()).build()
//                            .setCircuitBreakerConfig(CircuitBreakerConfig.custom().minimumNumberOfCalls(10).failureRateThreshold(20).build())
//                    , "helloworldCircuitBreaker");
//        };
//    }
//
//    @Bean
//    public Customizer<ReactiveResilience4JCircuitBreakerFactory> accountCircuitBreaker(){
//        return factory -> {
//            factory.configure(builder -> builder.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults()).build(), "accountCircuitBreaker");
//        };
//    }
//

}
