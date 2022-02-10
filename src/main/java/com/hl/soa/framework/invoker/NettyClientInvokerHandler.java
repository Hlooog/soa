package com.hl.soa.framework.invoker;

import com.hl.soa.framework.model.IResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author Hl
 * @create 2022/1/29 22:07
 */
public class NettyClientInvokerHandler extends SimpleChannelInboundHandler<IResponse> {

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, IResponse response) throws Exception {
        //将Netty异步返回的结果存入阻塞队列,以便调用端同步获取
        InvokerResponseHolder.putResultValue(response);
    }
}
