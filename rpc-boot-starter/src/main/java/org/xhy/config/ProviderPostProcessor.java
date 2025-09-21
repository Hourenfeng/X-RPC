package org.xhy.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.xhy.annotation.RpcService;
import org.xhy.common.Cache;
import org.xhy.common.URL;
import org.xhy.filter.FilterFactory;
import org.xhy.invoke.InvokerFactory;
import org.xhy.register.RegistryFactory;
import org.xhy.register.RegistryService;
import org.xhy.socket.serialization.SerializationFactory;
import org.xhy.socket.server.Server;
import org.xhy.utils.IpUtil;
import org.xhy.utils.ServiceNameBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ProviderPostProcessor implements InitializingBean, BeanPostProcessor, ApplicationContextAware {

    private RpcProperties rpcProperties;

    private final String ip = IpUtil.getIP();

    public ProviderPostProcessor(RpcProperties rpcProperties) {
        this.rpcProperties = rpcProperties;
    }

    private ExecutorService businessThreadPool;
    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        //初始化线程池
        businessThreadPool = new ThreadPoolExecutor(
                rpcProperties.getCorePollSize(),
                rpcProperties.getMaximumPoolSize(),
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(512),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        RegistryFactory.init();
        FilterFactory.initServer();
        InvokerFactory.init();
        SerializationFactory.init();
//        用线程的方式启动Server端
        Thread t = new Thread(() -> {
            final Server server = new Server(rpcProperties.getPort(), businessThreadPool);
            try {
                server.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
//        t.setDaemon(true);
        t.start();
    }

    public ExecutorService getBusinessThreadPool() {
        return businessThreadPool;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        // 找到bean上带有 RpcService 注解的类
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        if (rpcService != null) {
            // 可能会有多个接口,默认选择第一个接口
            String serviceName = beanClass.getInterfaces()[0].getName();
            if (!rpcService.serviceInterface().equals(void.class)){//看一下它有没有被rpc注解所标注
                serviceName = rpcService.serviceInterface().getName();
            }
            try {
                RegistryService registryService = RegistryFactory.get(rpcProperties.getRegistry().getName());
                final URL url = new URL();
                url.setPort(Properties.getPort());
                url.setIp(ip);
                url.setServiceName(serviceName);
                url.setVersion(rpcService.version());
                registryService.register(url);
                // 缓存
                final String key = ServiceNameBuilder.buildServiceKey(serviceName, rpcService.version());
                Cache.SERVICE_MAP.put(key,bean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bean;
    }
}
