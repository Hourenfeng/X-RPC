package org.xhy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 1、用于读取配置文件，在resources里。再通过依赖注入的方式给下一个Bean执行下面这个函数，把rpcProperties给Properties。
 * Properties vs RpcProperties：
 * Properties类也是配置信息，but是静态的；RpcProperties类也是配置信息。
 * Properties类是用来交给RPC使用的；RpcProperties类是用来交给Spring读取配置文件的。
 * 目的是解耦
 */
@Component
public class ProviderConfig {

    @Bean
    public RpcProperties rpcProperties(){
        return new RpcProperties();
    }

    @Bean
    public ProviderPostProcessor providerPostProcessor(RpcProperties rpcProperties){
        Properties.setPort(rpcProperties.getPort());
        Properties.setRegister(rpcProperties.getRegistry());
        Properties.setInvoke(rpcProperties.getInvoke());
        Properties.setSerialization(rpcProperties.getSerialization());
        Properties.setCorePollSize(rpcProperties.getCorePollSize());
        Properties.setMaximumPoolSize(rpcProperties.getMaximumPoolSize());
        return new ProviderPostProcessor(rpcProperties);
    }
}
