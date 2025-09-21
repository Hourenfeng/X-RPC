package org.xhy.register;

import org.xhy.common.Host;
import org.xhy.common.URL;

import java.io.IOException;
import java.util.List;

public interface RegistryService {

    void register(URL url) throws Exception;

    void unRegister(URL url) throws Exception;

    List<URL> discoveries(String serviceName, String version) throws Exception;

    void subscribe(URL url) throws Exception;

    void unSubscribe(URL url);

}
