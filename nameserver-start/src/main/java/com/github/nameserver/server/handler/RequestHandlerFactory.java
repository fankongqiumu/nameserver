package com.github.nameserver.server.handler;

import com.github.nameserver.exceptioin.NotSupportException;
import io.netty.handler.codec.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

public class RequestHandlerFactory {

    private RequestHandlerFactory(){
        throw NotSupportException.createInstanceNotSupportException();
    }

    public static final Map<HttpMethod, RequestHandler> REQUEST_HANDLERS = new HashMap<>();

    static {
        REQUEST_HANDLERS.put(HttpMethod.GET, new GetRequestHandler());
        REQUEST_HANDLERS.put(HttpMethod.POST, new PostRequestHandler());
    }

    public static void register(HttpMethod httpMethod, RequestHandler requestHandler) {

    }

    public static RequestHandler create(HttpMethod httpMethod) {
        return REQUEST_HANDLERS.get(httpMethod);
    }
}
