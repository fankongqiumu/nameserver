package com.github.nameserver.server;

import com.github.nameserver.server.handler.HttpServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//@Service
public class NettyServer implements InitializingBean, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int PORT = 8080;

    private  EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    @Autowired
    private HttpServerHandler httpServerHandler;


    @Override
    public void afterPropertiesSet() throws Exception {
         bossGroup = new NioEventLoopGroup(1);
         workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 是否开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 512)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast("decoder", new HttpRequestDecoder())
                                    .addLast("encoder", new HttpResponseEncoder())
                                    .addLast("aggregator", new HttpObjectAggregator(512 * 1024))
                                    .addLast("handler", httpServerHandler)
                            ;
                        }
                    });
            Channel ch = b.bind(PORT).sync().channel();
            logger.info("Netty Http Server started on port {}.", PORT);
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("occur exception when start server:", e);
        } finally {
            logger.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void destroy() throws Exception {
        if (null != workerGroup && !workerGroup.isShutdown()) {
            workerGroup.shutdownGracefully();
        }
        if (null != bossGroup && !bossGroup.isShutdown()) {
            bossGroup.shutdownGracefully();
        }
    }
}
