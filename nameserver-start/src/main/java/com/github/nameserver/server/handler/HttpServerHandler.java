package com.github.nameserver.server.handler;

import com.github.nameserver.domain.Result;
import com.github.nameserver.exceptioin.BadRequestException;
import com.github.nameserver.dispatcher.Dispatcher;
import com.github.nameserver.util.JsonUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.rtsp.RtspResponseStatuses.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.rtsp.RtspResponseStatuses.OK;

//@Component
@ChannelHandler.Sharable
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String FAVICON_ICO = "/favicon.ico";
    private static final AsciiString CONNECTION = AsciiString.cached("Connection");
    private static final AsciiString KEEP_ALIVE = AsciiString.cached("keep-alive");
    private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
    private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");

    @Autowired
    private Dispatcher dispatcher;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
        logger.info("Handle http request:{}", fullHttpRequest);
        String uri = fullHttpRequest.uri();
        if (uri.equals(FAVICON_ICO)) {
            return;
        }
        Result<?> result;
        FullHttpResponse response;
        try {
            Future<Result<?>> resultFuture = this.dispatcher.dispatcher(fullHttpRequest);
            result = resultFuture.get();
            String content = JsonUtils.toJsonString(result);
            byte[] responseBytes = content.getBytes(StandardCharsets.UTF_8);
            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(responseBytes));
            response.headers().set(CONTENT_TYPE, "application/json; charset=utf-8");
            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        } catch(BadRequestException badRequestException){
            HttpResponseStatus httpResponseStatus = badRequestException.getHttpResponseStatus();
            Result<Object> badRequestResult = Result.createFailWith(
                    String.valueOf(httpResponseStatus.code()),
                    httpResponseStatus.reasonPhrase()
            );
            String content = JsonUtils.toJsonString(badRequestResult);
            byte[] responseBytes = content.getBytes(StandardCharsets.UTF_8);
            response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(responseBytes));
            response.headers().set(CONTENT_TYPE, "application/json; charset=utf-8");
            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        } catch (Throwable e) {
            e.printStackTrace();
            String responseHtml = "<html><body>" + e + "</body></html>";
            byte[] responseBytes = responseHtml.getBytes(StandardCharsets.UTF_8);
            response = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR, Unpooled.wrappedBuffer(responseBytes));
            response.headers().set(CONTENT_TYPE, "text/html; charset=utf-8");
        }
        boolean keepAlive = HttpUtil.isKeepAlive(fullHttpRequest);
        if (!keepAlive) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.writeAndFlush(response);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
}
