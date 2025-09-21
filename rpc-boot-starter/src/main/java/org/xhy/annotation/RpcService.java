package org.xhy.annotation;

import org.springframework.stereotype.Component;
import org.xhy.common.constants.FaultTolerant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE;

@Retention(RetentionPolicy.RUNTIME)
@Target(value={TYPE})
public @interface RpcService {


    /**
     * 指定实现方,默认为实现接口中第一个
     * @return
     */
    Class<?> serviceInterface() default void.class;

    /**
     * 版本
     * @return
     */
    String version() default "1.0";
}
