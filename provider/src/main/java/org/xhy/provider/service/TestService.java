package org.xhy.provider.service;

import org.springframework.stereotype.Component;
import org.xhy.annotation.RpcService;
import org.xhy.service.HelloService;

@Component
@RpcService
public class TestService implements HelloService{
    @Override
    public Object hello(String arg) {
        String s = null;
        s.length();
        return arg+"provider1";
    }
}
