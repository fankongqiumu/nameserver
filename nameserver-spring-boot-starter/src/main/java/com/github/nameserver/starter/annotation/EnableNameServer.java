package com.github.nameserver.starter.annotation;


import com.github.nameserver.starter.config.NameServerClientConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(NameServerClientConfiguration.class)
public @interface EnableNameServer {
}
