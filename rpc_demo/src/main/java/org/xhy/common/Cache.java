package org.xhy.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Cache {

    public static ConcurrentHashMap<ServiceName, List<URL>> SERVICE_URLS = new ConcurrentHashMap<>();


    public static Map<Host, ChannelFuture> CHANNEL_FUTURE_MAP = new HashMap<Host, ChannelFuture>();

    public static List<URL> SUBSCRIBE_SERVICE_LIST = new ArrayList<>();

    public static Bootstrap BOOT_STRAP;

    public static Map<String,Object> SERVICE_MAP = new HashMap<>();
}
