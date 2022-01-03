package com.github.nameserver.annotation;


import com.github.nameserver.constant.HttpMethod;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mapping {
    String uri();

    HttpMethod httpMethod();
}
