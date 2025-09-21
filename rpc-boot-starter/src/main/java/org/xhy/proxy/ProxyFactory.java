package org.xhy.proxy;

import org.xhy.common.constants.RpcProxy;
import org.xhy.proxy.cglib.CgLibProxyFactory;
import org.xhy.spi.ExtensionLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProxyFactory {



    public static IProxy get(RpcProxy rpcProxy){
        return ExtensionLoader.getInstance().get(rpcProxy.name);

    }

    public static IProxy get(String name){
        return ExtensionLoader.getInstance().get(name);

    }

    public static void init() throws IOException, ClassNotFoundException {
        ExtensionLoader.getInstance().loadExtension(IProxy.class);
    }
}
