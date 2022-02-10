package com.hl.soa.framework.provider;

import com.hl.soa.framework.helper.PropertyConfigHelper;
import com.hl.soa.framework.model.IResponse;
import com.hl.soa.framework.serialization.NettyDecoderHandler;
import com.hl.soa.framework.serialization.NettyEncoderHandler;
import com.hl.soa.framework.serialization.serializer.ISerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;


/**
 * @author Hl
 * @create 2021/11/22 18:59
 */
public class NettyServer {

    private static final NettyServer nettyServer = new NettyServer();

    private Channel channel;
    // 服务端boss线程组
    private EventLoopGroup bossGroup;
    // 服务端worker线程组
    private EventLoopGroup workerGroup;
    // 序列化类型配置信息
    private Class<? extends ISerializer> clazz = PropertyConfigHelper.getSerialize();

    private NettyServer() {
    }

    public void start(final int port) {
        synchronized (NettyServer.class) {
            if (bossGroup != null && workerGroup != null) {
                return;
            }
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup(8);
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new NettyDecoderHandler(IResponse.class, clazz));
                            socketChannel.pipeline().addLast(new NettyEncoderHandler(clazz));
                            socketChannel.pipeline().addLast(new NettyServerInvokeHandler());
                        }
                    });
            try {
                channel = serverBootstrap.bind(port).sync().channel();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (channel == null) {
            throw new RuntimeException("netty server stoped");
        }

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        channel.closeFuture().syncUninterruptibly();

    }


    public static NettyServer getInstance() {
        return nettyServer;
    }
}
