package org.xhy.socket.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.xhy.common.RpcFuture;
import org.xhy.common.RpcRequestHolder;
import org.xhy.socket.codec.RpcProtocol;
import org.xhy.socket.codec.RpcResponse;

public class ClientHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcResponse> rpcResponseRpcProtocol) throws Exception {
        long requestId = rpcResponseRpcProtocol.getHeader().getRequestId();
        RpcFuture<RpcResponse> future = RpcRequestHolder.REQUEST_MAP.remove(requestId);
        future.getPromise().setSuccess(rpcResponseRpcProtocol.getBody());
    }
}