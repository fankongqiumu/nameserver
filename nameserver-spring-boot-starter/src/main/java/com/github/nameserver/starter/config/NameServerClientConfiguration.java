package com.github.nameserver.starter.config;

import com.github.nameserver.NameServerClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NameServerClientConfiguration {

    @Value("${nameserver.domain}")
    private String nameServerDomain;


    @Bean(name = "nameServerClient")
    public NameServerClient nameServerClient(){
        return NameServerClient.getInstance(nameServerDomain, true);
    }
}
