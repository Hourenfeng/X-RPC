package org.xhy.test.rpcinvoke;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.xhy.common.Cache;
import org.xhy.invoke.Invocation;
import org.xhy.invoke.Invoker;
import org.xhy.invoke.JdkReflectionInvoker;
import org.xhy.socket.codec.RpcRequest;
import org.xhy.utils.ServiceNameBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SpringBootTest
public class RpcInvokePerformanceTest {
    private static final int COUNT = 1_000_000;

    public static void main(String[] args) throws Exception {
        testPerformance();
    }


    public static void testPerformance() throws Exception {
        HelloService target = new HelloServiceImpl();

        System.out.println("开始测试...");

        long timeJavaAssist = testJavaAssist(target);
        long timeWithCache = testReflectWithCache(target);
        long timeWithoutCache = testJdkWithoutCache(target);

        System.out.println("JavaAssist 耗时: " + timeJavaAssist + " ms");
        System.out.println("反射（有缓存）耗时: " + timeWithCache + " ms");
        System.out.println("反射（无缓存）耗时: " + timeWithoutCache + " ms");
    }

    public static long testJavaAssist(HelloService target) throws Exception {
        target = new HelloServiceImpl();
        HelloService proxy = ProxyFactory.getProxy(HelloService.class, HelloServiceImpl.class, target);
//        HelloService proxy = (HelloService) ProxyFactory.getProxy(HelloServiceImpl.class, target);
        long start = System.currentTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            proxy.sayHello("hrf");
        }
        long end = System.currentTimeMillis();
        return end - start;
    }

    public static long testJdkWithoutCache(HelloService target) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setClassName(HelloService.class.getName());
        rpcRequest.setMethodName("sayHello");
        rpcRequest.setParameterTypes(String.class);
        rpcRequest.setParameter("hrf");
        rpcRequest.setServiceVersion("1.0");
        rpcRequest.setMethodCode(1);

        Invocation invocation = new Invocation(rpcRequest);
        // 模拟服务注册到 Cache
        String key = "org.xhy.test.rpcinvoke.HelloService" + "_1.0";
        org.xhy.common.Cache.SERVICE_MAP.put(key, target);

        long start = System.currentTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            // 模拟 Invoker.invoke()，但不使用缓存
            String className = invocation.getClassName();
            String methodName = invocation.getMethodName();
            Class<?> parameterType = invocation.getParameterTypes();
            Object parameter = invocation.getParameter();

            // 获取 Bean
            Object bean = Cache.SERVICE_MAP.get(key);

            // 获取方法（每次都重新获取）
            Method method = bean.getClass().getMethod(methodName, parameterType);

            // 调用方法（无缓存）
            method.invoke(bean, parameter);
        }
        long end = System.currentTimeMillis();
        return end - start;
    }

    // 使用 JdkReflectionInvoker（有缓存）
    public static long testReflectWithCache(HelloService target) throws Exception {
        Invoker invoker = new JdkReflectionInvoker();

        // 构造 Invocation
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setClassName("org.xhy.test.rpcinvoke.HelloService");
        rpcRequest.setMethodName("sayHello");
        rpcRequest.setParameter("Xhy");
        rpcRequest.setParameterTypes(String.class);
        rpcRequest.setServiceVersion("1.0");
        rpcRequest.setMethodCode(1);

        Invocation invocation = new Invocation(rpcRequest);

        // 构建缓存 Key
//        String key = "org.xhy.test.rpcinvoke.HelloService" + "_1.0";
        String key = ServiceNameBuilder.buildServiceKey(invocation.getClassName(), invocation.getServiceVersion());
        org.xhy.common.Cache.SERVICE_MAP.put(key, target);

        long start = System.currentTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            invoker.invoke(invocation);
        }
        long end = System.currentTimeMillis();
        return end - start;
    }
}

