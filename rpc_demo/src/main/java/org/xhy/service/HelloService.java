package org.xhy.service;

import org.xhy.annotation.RpcService;


@RpcService
public class HelloService implements IHelloService{

    @Override
    public Object hello(String text) {
        return "service1 result:"+text;
    }
}
