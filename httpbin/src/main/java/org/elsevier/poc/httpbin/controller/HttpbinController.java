package org.elsevier.poc.httpbin.controller;

import org.elsevier.poc.httpbin.service.HttpBinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/httpbin")
@Slf4j
public class HttpbinController {

    private HttpBinService httpBinService;

    public HttpbinController(HttpBinService httpBinService) {
        this.httpBinService = httpBinService;
    }

    @GetMapping("/get")
    public Mono<Map> get() {
        log.info("Call to httpbin get");
        return httpBinService.get();
    }

    @GetMapping("/delay/{seconds}")
    public Mono<Map> delay(@PathVariable int seconds) {
        log.info("Call to httpbin delay {}", seconds);
        return  httpBinService.delay(seconds);
    }

    @GetMapping("/fluxdelay/{seconds}")
    public Flux<String> fluxDelay(@PathVariable int seconds) {
        log.info("Call to httpbin fluxDelay {}", seconds);
        return httpBinService.fluxDelay(seconds);
    }
}