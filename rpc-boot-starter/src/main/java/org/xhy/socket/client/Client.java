package org.xhy.socket.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhy.common.*;
import org.xhy.common.constants.*;
import org.xhy.event.RpcListerLoader;
import org.xhy.filter.FilterFactory;
import org.xhy.proxy.IProxy;
import org.xhy.proxy.ProxyFactory;
import org.xhy.register.RegistryFactory;
import org.xhy.register.RegistryService;
import org.xhy.router.LoadBalancerFactory;
import org.xhy.socket.codec.*;
import org.xhy.tolerant.FaultTolerantFactory;

import java.io.IOException;
import java.util.List;

public class Client {

    private  Bootstrap bootstrap;
    private  EventLoopGroup eventLoopGroup;

    public void run(){
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new RpcEncoder())
                                .addLast(new RpcDecoder())
                                .addLast(new ClientHandler());
                    }
                });

        Cache.BOOT_STRAP = bootstrap;
    }


    public void connectServer() throws Exception {
        for(URL url: Cache.SUBSCRIBE_SERVICE_LIST){
            final RegistryService registryService = RegistryFactory.get(Register.ZOOKEEPER);
            final List<URL> urls = registryService.discoveries(url.getServiceName(), url.getVersion());
            if (!urls.isEmpty()){
                for (URL u : urls) {
                    final ChannelFuture connect = bootstrap.connect(u.getIp(), u.getPort());
                    Cache.CHANNEL_FUTURE_MAP.put(new Host(u.getIp(),u.getPort()),connect);
                }
            }
        }
    }

    public void init() throws IOException, ClassNotFoundException {
        new RpcListerLoader().init();
        FaultTolerantFactory.init();
        RegistryFactory.init();
        FilterFactory.initClient();
        ProxyFactory.init();
        LoadBalancerFactory.init();
    }


}
