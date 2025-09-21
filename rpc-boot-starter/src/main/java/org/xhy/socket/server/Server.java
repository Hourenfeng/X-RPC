package org.xhy.socket.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.xhy.config.Properties;
import org.xhy.filter.FilterFactory;
import org.xhy.invoke.InvokerFactory;
import org.xhy.register.RegistryFactory;
import org.xhy.socket.codec.RpcDecoder;
import org.xhy.socket.codec.RpcEncoder;
import org.xhy.socket.serialization.SerializationFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

public class Server {

    private Integer port;
    private final ExecutorService businessThreadPool;

    private ServerBootstrap bootstrap;

    public Server(Integer port, ExecutorService businessThreadPool) {
        this.port = port;
        this.businessThreadPool = businessThreadPool;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            bootstrap= new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new RpcEncoder());
                            ch.pipeline().addLast(new RpcDecoder());
                            ch.pipeline().addLast(new ServerHandler(businessThreadPool));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            if (port == null){
                bootstrap.bind(0).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            Channel channel = channelFuture.channel();
                            InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
                            Properties.setPort(localAddress.getPort());
                        }
                    }
                }).sync().channel().closeFuture().sync();
            }else{
                bootstrap.bind(port).sync().channel().closeFuture().sync();
            }
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public void init() throws IOException, ClassNotFoundException {
        RegistryFactory.init();
        FilterFactory.initServer();
        InvokerFactory.init();
        SerializationFactory.init();
    }

}
