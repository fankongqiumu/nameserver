package com.github.nameserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync(proxyTargetClass = true)
@SpringBootApplication
public class NameServerStart {
    public static void main(String[] args) {
        SpringApplication.run(NameServerStart.class, args);
    }
}
