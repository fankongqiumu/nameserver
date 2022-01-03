package com.github.nameserver.exceptioin;

import io.netty.handler.codec.http.HttpResponseStatus;

public class BadRequestException extends RuntimeException {

    private HttpResponseStatus httpResponseStatus;

    public BadRequestException(HttpResponseStatus  httpResponseStatus) {
        super(httpResponseStatus.reasonPhrase());
        this.httpResponseStatus = httpResponseStatus;
    }

    public HttpResponseStatus getHttpResponseStatus() {
        return httpResponseStatus;
    }
}
