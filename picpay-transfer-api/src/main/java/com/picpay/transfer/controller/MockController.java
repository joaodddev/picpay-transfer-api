package com.picpay.transfer.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mock")
@Profile("mock")
public class MockController {

    @GetMapping("/authorize")
    public Map<String, Object> authorize() {
        return Map.of(
                "status", "success",
                "data", Map.of("authorization", true)
        );
    }
}