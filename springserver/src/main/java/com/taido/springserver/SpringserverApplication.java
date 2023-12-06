package com.taido.springserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class SpringserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringserverApplication.class, args);
    }

}
