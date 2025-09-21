package org.xhy.proxy.cglib;

import net.sf.cglib.proxy.Enhancer;
import org.xhy.annotation.RpcReference;
import org.xhy.common.constants.FaultTolerant;
import org.xhy.common.constants.LoadBalance;
import org.xhy.proxy.IProxy;

import java.util.concurrent.TimeUnit;

public class CgLibProxyFactory<T> implements IProxy {


    public <T> T getProxy(Class claz, RpcReference rpcReference)  {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(claz);
        enhancer.setCallback(new CgLibProxy(claz,rpcReference));
        return (T) enhancer.create();
    }
}
