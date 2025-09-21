package org.xhy.service;

import org.xhy.annotation.RpcService;


@RpcService
public class HelloService2 implements IHelloService{

    @Override
    public Object hello(String text) {
        String s = null;
        s.length();
        return "service2 result:"+text;
    }
}
