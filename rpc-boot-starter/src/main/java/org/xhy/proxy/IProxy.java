package org.xhy.proxy;

import org.xhy.annotation.RpcReference;
import org.xhy.common.constants.FaultTolerant;
import org.xhy.common.constants.LoadBalance;

import java.util.concurrent.TimeUnit;

public interface IProxy {

    <T> T getProxy(Class claz, RpcReference rpcReference);
}
