package com.hl.soa.framework.provider;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.hl.soa.framework.helper.SpringBeanHelper;
import com.hl.soa.framework.model.IRequest;
import com.hl.soa.framework.model.IResponse;
import com.hl.soa.framework.model.ProviderService;
import com.hl.soa.framework.zookeeper.IRegisterCenter4Provider;
import com.hl.soa.framework.zookeeper.RegisterCenter;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * TODO netty服务
 *
 * @author Hl
 * @create 2021/11/23 20:51
 */
@ChannelHandler.Sharable
public class NettyServerInvokeHandler extends SimpleChannelInboundHandler<IRequest> {

    //服务端限流
    private static final Map<String, Semaphore> serviceKeySemaphoreMap = Maps.newConcurrentMap();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //发生异常,关闭链路
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, IRequest request) throws Exception {

        if (channelHandlerContext.channel().isWritable()) {
            ProviderService providerService = request.getProviderService();
            long timeout = request.getInvokeTimeout();
            final String methodName = request.getInvokedMethodName();
            Object[] args = request.getArgs();
            // 根据方法名定位到具体某一个服务提供者
            String serviceKey = providerService.getServiceItf().getName();
            //获取限流工具类
            int workerThread = providerService.getWorkerThread();
            Semaphore semaphore = serviceKeySemaphoreMap.get(serviceKey);
            if (semaphore == null) {
                synchronized (serviceKeySemaphoreMap) {
                    semaphore = serviceKeySemaphoreMap.get(serviceKey);
                    if (semaphore == null) {
                        semaphore = new Semaphore(workerThread);
                        serviceKeySemaphoreMap.put(serviceKey, semaphore);
                    }
                }
            }
            boolean acquire = false;
            Object result = null;

            try {
                acquire = semaphore.tryAcquire(timeout, TimeUnit.MICROSECONDS);
                if (acquire) {
                    Object serviceObject = SpringBeanHelper.getBean(providerService.getServiceItf());
                    Method method = serviceObject.getClass().getMethod(methodName, convert(args));
                    result = method.invoke(serviceObject, args);
                }
            } catch (Exception e) {
                result = e;
            } finally {
                if (acquire) {
                    semaphore.release();
                }
            }
            //根据服务调用结果组装调用返回对象
            IResponse response = new IResponse();
            response.setInvokeTimeout(timeout);
            response.setUniqueKey(request.getUniqueKey());
            response.setResult(result);

            //将服务调用返回对象回写到消费端
            channelHandlerContext.writeAndFlush(response);
        }
    }

    private Class<?>[] convert(Object[] args) {
        if (args == null) return null;
        Class<?>[] clazz = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) clazz[i] = null;
            else clazz[i] = args[i].getClass();
        }
        return clazz;
    }
}
