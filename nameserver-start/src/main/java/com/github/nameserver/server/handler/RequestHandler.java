package com.github.nameserver.server.handler;

import io.netty.handler.codec.http.FullHttpRequest;

public interface RequestHandler {
    Object[] handle(FullHttpRequest fullHttpRequest, String[] parameterNames, Class<?>[] parameterTypes);


}
