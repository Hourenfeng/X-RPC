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

    public static void init() throws IOException, ClassNotFoundException {
        final ExtensionLoader extensionLoader = ExtensionLoader.getInstance();
        extensionLoader.loadExtension(FaultTolerantStrategy.class);
    }
}
