package com.hl.soa.framework.invoker;

import com.hl.soa.framework.model.IRequest;
import com.hl.soa.framework.model.IResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author Hl
 * @create 2022/1/30 23:02
 */
public class InvokerServiceCallable implements Callable<IResponse> {

    private Channel channel;
    private InetSocketAddress socketAddress;
    private IRequest request;


    public InvokerServiceCallable(InetSocketAddress socketAddress, IRequest request) {
        this.socketAddress = socketAddress;
        this.request = request;
    }

    public static InvokerServiceCallable of(InetSocketAddress socketAddress, IRequest request) {
        return new InvokerServiceCallable(socketAddress, request);
    }

    @Override
    public IResponse call() throws Exception {

        // 初始化返回结果容器，将本次调用的唯一标识作为key存入返回结果的map
        InvokerResponseHolder.initResponseData(request.getUniqueKey());
        ArrayBlockingQueue<Channel> acquire = NettyChannelPoolFactory.getInstance().acquire(socketAddress);
        try {
            if (channel == null) {
                // 从队列中获取本次调用的Netty channel
                channel = acquire.poll(request.getInvokeTimeout(), TimeUnit.MICROSECONDS);
            }
            // 获取的channel 不可用
            while (!channel.isOpen() || !channel.isActive() || !channel.isWritable()) {
                channel = acquire.poll(request.getInvokeTimeout(), TimeUnit.MICROSECONDS);
                if (channel == null) {
                    channel = NettyChannelPoolFactory.getInstance().registerChannel(socketAddress);
                }
            }
            ChannelFuture channelFuture = channel.writeAndFlush(request);
            channelFuture.syncUninterruptibly();
            long invokeTimeout = request.getInvokeTimeout();
            return InvokerResponseHolder.getValue(request.getUniqueKey(), invokeTimeout);
        } finally {
            // 调用完以后 将channel释放重新放回容器
            NettyChannelPoolFactory.getInstance().release(acquire, channel, socketAddress);
        }
    }
}
