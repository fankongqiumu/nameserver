package com.github.nameserver.starter.config;

import com.github.nameserver.NameServerClient;
import com.github.nameserver.constant.Constants;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

@Configuration
public class NameServerClientConfiguration implements EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    @Bean(name = Constants.BeanNameConstants.NAMESERVER_CLIENT)
    public NameServerClient nameServerClient(){
        String property = environment.getRequiredProperty(Constants.NAMESERVER_MOMAIN);
        List<String> domains = Arrays.asList(property.split(Constants.DEFAULT_DELIMITER));
        return NameServerClient.getInstance(domains, true);
    }
}
