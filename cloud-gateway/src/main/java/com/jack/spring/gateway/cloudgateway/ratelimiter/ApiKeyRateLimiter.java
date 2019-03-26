package com.jack.spring.gateway.cloudgateway.ratelimiter;

import com.jack.spring.gateway.cloudgateway.ratelimiter.service.Throttle;
import com.jack.spring.gateway.cloudgateway.ratelimiter.service.ThrottleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.redis.core.script.RedisScript;

@Primary
@Component
@Slf4j
public class ApiKeyRateLimiter implements RateLimiter {

    @Autowired
    private ThrottleService throttleService;

    // How many tokens are added to bucket
    private static final int REPLENISH_RATE = 1;

    // How big is the bucket
    private static final int BURST_CAPACITY = 1;

    private final RedisRateLimiter rateLimiter;
    private final RedisScript<List<Long>> script;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Autowired
    public ApiKeyRateLimiter(
            RedisRateLimiter rateLimiter,
            @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> script,
            ReactiveRedisTemplate<String, String> redisTemplate) {

        this.rateLimiter = rateLimiter;
        this.script = script;
        this.redisTemplate = redisTemplate;
    }

    private int getBurstCapacity(String routeId, String apiKey) {
        Throttle throttle = throttleService.getThrottleMap().get(apiKey);
        return throttle.getBurstRate();

    }
    private int getReplenishRate(String routeId, String apiKey) {

        Throttle throttle = throttleService.getThrottleMap().get(apiKey);
        return throttle.getReplenishRate();

    }

    public Mono<Response> isAllowed(String routeId, String apiKey) {

        int replenishRate = getReplenishRate(routeId, apiKey);
        int burstCapacity = getBurstCapacity(routeId, apiKey);

        try {
            List<String> keys = getKeys(apiKey);

            // The arguments to the LUA script. time() returns unixtime in seconds.
            List<String> scriptArgs = Arrays.asList(replenishRate + "", burstCapacity + "",
                    Instant.now().getEpochSecond() + "", "1");
            Flux<List<Long>> flux = this.redisTemplate.execute(this.script, keys, scriptArgs);

            return flux.onErrorResume(throwable -> Flux.just(Arrays.asList(1L, -1L)))
                    .reduce(new ArrayList<Long>(), (longs, l) -> {
                        longs.addAll(l);
                        return longs;
                    }) .map(results -> {
                        boolean allowed = results.get(0) == 1L;
                        Long tokensLeft = results.get(1);

                        Response response = new Response(allowed, getHeaders(tokensLeft, replenishRate, burstCapacity));

                        if (log.isDebugEnabled()) {
                            log.debug("response: " + response);
                        }
                        return response;
                    });
        }
        catch (Exception e) {
            /*
             * We don't want a hard dependency on Redis to allow traffic. Make sure to set
             * an alert so you know if this is happening too much. Stripe's observed
             * failure rate is 0.01%.
             */
            log.error("Error determining if user allowed from redis", e);
        }
        return Mono.just(new Response(true, getHeaders(-1L, replenishRate, burstCapacity)));
    }

    private static List<String> getKeys(String id) {
        String prefix = "request_rate_limiter.{" + id;
        String tokenKey = prefix + "}.tokens";
        String timestampKey = prefix + "}.timestamp";
        return Arrays.asList(tokenKey, timestampKey);
    }

    private HashMap<String, String> getHeaders(Long tokensLeft, Integer replenish, Integer burst) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(RedisRateLimiter.REMAINING_HEADER, tokensLeft.toString());
        headers.put(RedisRateLimiter.REPLENISH_RATE_HEADER, replenish.toString());
        headers.put(RedisRateLimiter.BURST_CAPACITY_HEADER, burst.toString());
        return headers;
    }

    @Override
    public Map getConfig() {
        return rateLimiter.getConfig();
    }

    @Override
    public Class getConfigClass() {
        return rateLimiter.getConfigClass();
    }

    @Override
    public Object newConfig() {
        return rateLimiter.newConfig();
    }
}
