package org.xhy.router;

import org.xhy.common.constants.LoadBalance;
import org.xhy.register.RegistryService;
import org.xhy.spi.ExtensionLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoadBalancerFactory {


    public static LoadBalancer get(LoadBalance loadBalance){
        return ExtensionLoader.getInstance().get(loadBalance.name);
    }

    public static LoadBalancer get(String name){
        return ExtensionLoader.getInstance().get(name);
    }

    public static void init() throws IOException, ClassNotFoundException {
        ExtensionLoader.getInstance().loadExtension(LoadBalancer.class);
    }
}

