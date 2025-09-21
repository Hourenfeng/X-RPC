package org.xhy.router;

import org.xhy.common.URL;

import java.util.List;

public interface LoadBalancer {

    URL select(List<URL> urls);

}
