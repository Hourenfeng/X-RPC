package org.xhy.annotation;

import org.xhy.common.constants.FaultTolerant;
import org.xhy.common.constants.LoadBalance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.TYPE})
public @interface RpcReference {

    String version() default "1.0";

    FaultTolerant faultTolerant() default FaultTolerant.Failover;

    LoadBalance loadBalance() default LoadBalance.Round;//可以自定义负载均衡算法

    long time() default 3000;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
