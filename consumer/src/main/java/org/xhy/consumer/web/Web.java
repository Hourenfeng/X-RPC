package org.xhy.consumer.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.annotation.RpcReference;
import org.xhy.common.constants.LoadBalance;
import org.xhy.service.HelloService;

@RestController
@RequestMapping
public class Web {

    @RpcReference
    HelloService helloService;

    @GetMapping
    public Object hello(String arg){
        return helloService.hello(arg);
    }
}
