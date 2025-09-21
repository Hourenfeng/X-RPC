package org.xhy.event;

import io.netty.channel.ChannelFuture;
import org.xhy.common.Cache;
import org.xhy.common.Host;
import org.xhy.common.ServiceName;
import org.xhy.common.URL;

import java.util.ArrayList;

public class DestroyRpcLister implements IRpcLister<DestroyEventData>{
    @Override
    public void exec(DestroyEventData destroyEventData) {
        final URL url = (URL) destroyEventData.getData();
        final ServiceName serviceName = new ServiceName(url.getServiceName(), url.getVersion());
        if (Cache.SERVICE_URLS.containsKey(serviceName)){

            Cache.SERVICE_URLS.get(serviceName).remove(url);
        }
        final Host ip = new Host(url.getIp(), url.getPort());
        if (Cache.CHANNEL_FUTURE_MAP.containsKey(ip)) {
            Cache.CHANNEL_FUTURE_MAP.remove(ip);
        }
    }
}
