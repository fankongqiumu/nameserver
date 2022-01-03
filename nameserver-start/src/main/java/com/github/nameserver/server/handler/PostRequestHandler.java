package com.github.nameserver.server.handler;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostRequestHandler implements RequestHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Object handle(FullHttpRequest fullHttpRequest) {
        String requestUri = fullHttpRequest.uri();
        logger.info("request uri :[{}]", requestUri);
        String contentType = this.getContentType(fullHttpRequest.headers());
        if (contentType.equals("application/json")) {
            return fullHttpRequest.content().toString(Charsets.toCharset(CharEncoding.UTF_8));
        } else {
            throw new IllegalArgumentException("only receive application/json type data");
        }

    }

    private String getContentType(HttpHeaders headers) {
        String typeStr = headers.get("Content-Type");
        String[] list = typeStr.split(";");
        return list[0];
    }

    @Override
    public Object[] handle(FullHttpRequest fullHttpRequest, String[] parameterNames, Class<?>[] parameterTypes) {
        return new Object[0];
    }
}
