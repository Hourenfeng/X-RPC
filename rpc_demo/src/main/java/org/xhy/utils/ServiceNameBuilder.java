package org.xhy.utils;

public class ServiceNameBuilder {
    public static String buildServiceKey(String serviceName, String serviceVersion) {
        return String.join("$", serviceName, serviceVersion);
    }


}
