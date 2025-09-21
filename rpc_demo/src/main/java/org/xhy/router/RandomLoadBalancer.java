package org.xhy.router;

import org.xhy.common.URL;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class RandomLoadBalancer implements LoadBalancer {
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public URL select(List<URL> urls) {
        System.out.println("使用随机算法进行负载均衡");
        return urls.get(random.nextInt(urls.size()));
    }
}
