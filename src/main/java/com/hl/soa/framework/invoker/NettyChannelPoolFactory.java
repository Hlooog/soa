package com.hl.soa.framework.invoker;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hl.soa.framework.helper.PropertyConfigHelper;
import com.hl.soa.framework.model.IResponse;
import com.hl.soa.framework.model.ProviderService;
import com.hl.soa.framework.serialization.NettyDecoderHandler;
import com.hl.soa.framework.serialization.NettyEncoderHandler;
import com.hl.soa.framework.serialization.serializer.ISerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.collections.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * @author Hl
 * @create 2022/1/23 15:21
 */
public class NettyChannelPoolFactory {

    private static final NettyChannelPoolFactory pool = new NettyChannelPoolFactory();

    //Key为服务提供者地址,value为Netty Channel阻塞队列
    private static final Map<InetSocketAddress, ArrayBlockingQueue<Channel>> channelPoolMap = Maps.newConcurrentMap();
    //初始化Netty Channel阻塞队列的长度,该值为可配置信息
    private static final int channelConnectSize = PropertyConfigHelper.getChannelConnectionSize();
    //初始化序列化协议类型,该值为可配置信息
    private static final Class<? extends ISerializer> serializeType = PropertyConfigHelper.getSerialize();
    //服务提供者列表
    private List<ProviderService> serviceMetaDataList = Lists.newArrayList();


    private NettyChannelPoolFactory() {
    }


    /**
     * 初始化Netty channel 连接队列Map
     *
     * @param providerMap
     */
    public void initChannelPoolFactory(Map<String, List<ProviderService>> providerMap) {
        // 将服务提供者信息储存到本地
        Collection<List<ProviderService>> collectionServiceMetaDataList = providerMap.values();
        for (List<ProviderService> providerServices : collectionServiceMetaDataList) {
            if (CollectionUtils.isEmpty(providerServices)) continue;
            serviceMetaDataList.addAll(providerServices);
        }
        // 获取服务提供者地址列表
        HashSet<InetSocketAddress> socketAddressSet = Sets.newHashSet();
        for (ProviderService providerService : serviceMetaDataList) {
            String serverIp = providerService.getServerIp();
            int serverPort = providerService.getServerPort();
            InetSocketAddress socketAddress = new InetSocketAddress(serverIp, serverPort);
            socketAddressSet.add(socketAddress);
        }
        // 根据服务提供者地址列表初始化Channel阻塞队列,并以地址为Key,地址对应的Channel阻塞队列为value,存入channelPoolMap
        for (InetSocketAddress socketAddress : socketAddressSet) {
            try {
                int realChannelConnectSize = 0;
                while (realChannelConnectSize < channelConnectSize) {
                    Channel channel = null;
                    while (channel == null) {
                        // 若channel不存在,则注册新的Netty Channel
                        channel = registerChannel(socketAddress);
                    }
                    // 计数器,初始化的时候存入阻塞队列的Netty Channel个数不超过channelConnectSize
                    realChannelConnectSize++;
                    // 将新注册的Netty Channel存入阻塞队列channelArrayBlockingQueue
                    // 并将阻塞队列channelArrayBlockingQueue作为value存入channelPoolMap
                    ArrayBlockingQueue<Channel> channelArrayBlockingQueue = channelPoolMap.get(socketAddress);
                    if (channelArrayBlockingQueue == null) {
                        channelArrayBlockingQueue = new ArrayBlockingQueue<Channel>(channelConnectSize);
                        channelPoolMap.put(socketAddress, channelArrayBlockingQueue);
                    }
                    channelArrayBlockingQueue.offer(channel);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 为服务提供者地址socketAddress注册新的Channel
     *
     * @param socketAddress
     * @return
     */
    public Channel registerChannel(InetSocketAddress socketAddress) {
        try {
            NioEventLoopGroup group = new NioEventLoopGroup(10);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.remoteAddress(socketAddress);
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new NettyEncoderHandler(serializeType));
                            socketChannel.pipeline().addLast(new NettyDecoderHandler(IResponse.class, serializeType));
                            socketChannel.pipeline().addLast(new NettyClientInvokerHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect().sync();
            final Channel channel = channelFuture.channel();
            final CountDownLatch countDownLatch = new CountDownLatch(1);

            final List<Boolean> isSuccessHolder = Lists.newArrayListWithCapacity(1);
            // 监听channel是否建立成功
            channelFuture.addListener(future -> {
                if (future.isSuccess()) {
                    isSuccessHolder.add(Boolean.TRUE);
                } else {
                    // 建立失败 保存标记
                    future.cause().printStackTrace();
                    isSuccessHolder.add(Boolean.FALSE);
                }
                countDownLatch.countDown();
            });
            countDownLatch.await();
            if (isSuccessHolder.get(0)) {
                return channel;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    /**
     * 根据服务提供者地址获取对应的Netty Channel阻塞队列
     *
     * @param socketAddress
     * @return
     */
    public ArrayBlockingQueue<Channel> acquire(InetSocketAddress socketAddress) {
        return channelPoolMap.get(socketAddress);
    }


    /**
     * Channel使用完毕之后,回收到阻塞队列arrayBlockingQueue
     *
     * @param arrayBlockingQueue
     * @param channel
     * @param inetSocketAddress
     */
    public void release(ArrayBlockingQueue<Channel> arrayBlockingQueue, Channel channel, InetSocketAddress inetSocketAddress) {
        if (arrayBlockingQueue == null) return;
        // 回收之前先检查是否可用，不可用的话，重新注册
        if (channel == null || !channel.isOpen() || !channel.isActive() || !channel.isWritable()) {
            if (channel != null) {
                channel.deregister().syncUninterruptibly().awaitUninterruptibly();
                channel.closeFuture().syncUninterruptibly().awaitUninterruptibly();
            }
            Channel newChannel = null;
            while (newChannel == null) {
                newChannel = registerChannel(inetSocketAddress);
            }
            arrayBlockingQueue.offer(newChannel);
            return;
        }
        arrayBlockingQueue.offer(channel);
    }

    public static NettyChannelPoolFactory getInstance() {
        return pool;
    }


}
