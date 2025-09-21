package org.xhy.socket.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.xhy.common.constants.MsgType;
import org.xhy.common.constants.RpcInvoker;
import org.xhy.config.Properties;
import org.xhy.filter.*;
import org.xhy.invoke.Invocation;
import org.xhy.invoke.Invoker;
import org.xhy.invoke.InvokerFactory;
import org.xhy.socket.codec.MsgHeader;
import org.xhy.socket.codec.RpcProtocol;
import org.xhy.socket.codec.RpcRequest;
import org.xhy.socket.codec.RpcResponse;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class ServerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {
    private final ExecutorService businessThreadPool;

    public ServerHandler(ExecutorService businessThreadPool) {
        this.businessThreadPool = businessThreadPool;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcRequest> rpcProtocol) throws Exception {
        businessThreadPool.submit(() -> {
            final RpcRequest rpcRequest = rpcProtocol.getBody();
            final RpcResponse response = new RpcResponse();
            final RpcProtocol<RpcResponse> resRpcProtocol = new RpcProtocol();
            final MsgHeader header = rpcProtocol.getHeader();
            header.setMsgType((byte) MsgType.RESPONSE.ordinal());
            resRpcProtocol.setHeader(header);
            final Invoker invoker = InvokerFactory.get(Properties.getInvoke());
            try {
                final List<Filter> serverBeforeFilters = FilterFactory.getServerBeforeFilters();
                if (!serverBeforeFilters.isEmpty()) {
                    final FilterData<RpcRequest> rpcRequestFilterData = new FilterData<>(rpcRequest);
                    final FilterLoader filterLoader = new FilterLoader();
                    filterLoader.addFilter(serverBeforeFilters);
                    final FilterResponse filterResponse = filterLoader.doFilter(rpcRequestFilterData);
                    if (!filterResponse.getResult()) {
                        throw filterResponse.getException();
                    }
                }
                // 执行
                final Object data = invoker.invoke(new Invocation(rpcRequest));
                response.setData(data);
            } catch (Exception e) {
                response.setException(e);
            } finally {
                final List<Filter> serverAfterFilters = FilterFactory.getServerAfterFilters();
                if (!serverAfterFilters.isEmpty()) {
                    final FilterData<RpcResponse> rpcResponseFilterData = new FilterData<>(response);
                    final FilterLoader filterLoader = new FilterLoader();
                    filterLoader.addFilter(serverAfterFilters);
                    final FilterResponse filterResponse = filterLoader.doFilter(rpcResponseFilterData);
                    if (!filterResponse.getResult()) {
                        try {
                            throw filterResponse.getException();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            resRpcProtocol.setBody(response);
            channelHandlerContext.writeAndFlush(resRpcProtocol);
        });


    }
}