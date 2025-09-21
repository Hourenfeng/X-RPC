package org.xhy.test.rpcinvoke;

public class HelloServiceImpl implements HelloService{
    @Override
    public String sayHello(String name) {
        return "HELLO : " + name;
    }
}
