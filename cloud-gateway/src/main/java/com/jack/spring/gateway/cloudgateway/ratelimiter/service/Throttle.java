package com.jack.spring.gateway.cloudgateway.ratelimiter.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Throttle {

    private int burstRate;
    private int replenishRate;
}
