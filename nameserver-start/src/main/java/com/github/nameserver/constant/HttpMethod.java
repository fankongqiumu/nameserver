package com.github.nameserver.constant;

public enum HttpMethod {
    GET(io.netty.handler.codec.http.HttpMethod.GET),
    POST(io.netty.handler.codec.http.HttpMethod.POST),
    ;
    private io.netty.handler.codec.http.HttpMethod  httpMethod;

    private HttpMethod(io.netty.handler.codec.http.HttpMethod  httpMethod){
        this.httpMethod = httpMethod;
    }

    public io.netty.handler.codec.http.HttpMethod getHttpMethod() {
        return httpMethod;
    }
}
