package org.xhy.tolerant;

import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import org.xhy.common.*;
import org.xhy.socket.codec.RpcResponse;
import org.xhy.utils.CheckedFunction;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class FailRetryFaultTolerantStrategy implements FaultTolerantStrategy{
    private final FailoverRetryPolicy retryPolicy =
            new FailoverRetryPolicy(3, 200);   // 每个 URL 3 次，每次间隔 200ms

    @Override
    public Object handler(FaultContext ctx) throws Exception {

        List<URL> urls = ctx.getUrls();

        // 封装发送逻辑
        CheckedFunction<URL, Object> action = url -> {
            System.out.println("正在尝试调用：" + url);
            //  更新当前 URL（供链路追踪）
            ctx.setCurrentURL(url);

            // 发送请求
            ChannelFuture cf = Cache.CHANNEL_FUTURE_MAP
                    .get(new Host(url.getIp(), url.getPort()));
            cf.channel().writeAndFlush(ctx.getRpcProtocol());

            RpcFuture<RpcResponse> future = new RpcFuture(
                    new DefaultPromise(new DefaultEventLoop()), 3000);
            RpcRequestHolder.REQUEST_MAP.put(ctx.getRequestId(), future);

            RpcResponse resp = future.getPromise().sync()
                    .get(future.getTimeout(), TimeUnit.MILLISECONDS);

            // 出错则抛异常，让 Retryer 捕获
            if (resp.getException() != null) {
                throw resp.getException();
            }
            return resp.getData();
        };

        return retryPolicy.execute(urls, action);
    }
}
