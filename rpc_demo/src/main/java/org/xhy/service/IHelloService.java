package org.xhy.service;

import org.xhy.annotation.RpcReference;

@RpcReference
public interface IHelloService {

    Object hello(String text);
}
