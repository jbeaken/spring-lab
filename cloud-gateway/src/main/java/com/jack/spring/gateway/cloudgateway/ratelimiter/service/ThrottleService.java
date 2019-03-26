package com.jack.spring.gateway.cloudgateway.ratelimiter.service;

import lombok.*;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ThrottleService {

    @Getter
    private Map<String, Throttle> throttleMap;

}
