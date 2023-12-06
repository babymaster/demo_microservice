package com.tdn.demometer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestMeter {
    private static Logger logger = LoggerFactory.getLogger(TestMeter.class);
    @GetMapping("/")
    public String getMeter() {
        logger.debug("Hello getMeter()");
        return "success";
    }
}
