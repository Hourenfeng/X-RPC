package org.xhy.filter.client;

import org.xhy.filter.Filter;
import org.xhy.filter.FilterData;
import org.xhy.socket.codec.RpcRequest;

public interface ClientBeforeFilter extends Filter<RpcRequest> {
}
