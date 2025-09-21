package org.xhy.tolerant;

import org.xhy.common.constants.FaultTolerant;
import org.xhy.spi.ExtensionLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FaultTolerantFactory {


    public static FaultTolerantStrategy get(FaultTolerant faultTolerant){
        final String name = faultTolerant.name;
        return ExtensionLoader.getInstance().get(name);
    }

    public static FaultTolerantStrategy get(String name){
        return ExtensionLoader.getInstance().get(name);
    }

//    通过SPI初始化，放到缓存里，之后会通过上面的get函数进行初始化
    public static void init() throws IOException, ClassNotFoundException {
        final ExtensionLoader extensionLoader = ExtensionLoader.getInstance();
        extensionLoader.loadExtension(FaultTolerantStrategy.class);
    }
}
