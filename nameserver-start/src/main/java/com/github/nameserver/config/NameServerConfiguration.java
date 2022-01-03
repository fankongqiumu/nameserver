package com.github.nameserver.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//@Configuration
public class NameServerConfiguration {

    @Bean(name = "parameterNameDiscoverer")
    @ConditionalOnMissingBean
    public LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer(){
        return new LocalVariableTableParameterNameDiscoverer();
    }

    @Bean(name = "targetMethodExecuteService")
    public ExecutorService targetMethodExecuteService(){
        return new ThreadPoolExecutor(50, 100, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(2048));
    }
}
