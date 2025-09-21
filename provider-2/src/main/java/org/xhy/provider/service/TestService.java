package org.xhy.provider.service;

import org.springframework.stereotype.Component;
import org.xhy.annotation.RpcService;
import org.xhy.service.HelloService;


/**这个@Component也可以直接加在“rpc-boot-starter/src/main/java/org/xhy/annotation/RpcService.java”，但是
 * 由于我想解耦，不想RPC和Spring进行强绑定。如果加在“rpc-boot-starter/src/main/java/org/xhy/annotation/RpcService.java”这里，现在脱离了Spring框架，要在RPCService里嵌套Component咋办呢？
 * 就很难。
 * 正确的处理方式：在写组件项目时，最好不要和Spring这类其他的依赖，有过多的参与，如果可以就用底层依赖的组件，EG：JDK、Maven等
 *              RPC提供一个包扫描，去扫描这个项目下所有类，看这些类上面有没有RPCService这个注解，看对应的类下面的字段上面有没有RPCReference这个注解，再进行相关的注册。
 * 现在是为了简单。
 */
@Component
@RpcService
public class TestService implements HelloService{
    @Override
    public Object hello(String arg) {
         System.out.println("当前线程：" + Thread.currentThread().getName());
        return arg + "provider2";
    }
}
