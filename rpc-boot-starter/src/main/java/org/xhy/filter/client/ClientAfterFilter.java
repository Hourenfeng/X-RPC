package org.xhy.filter.client;

import org.xhy.filter.Filter;
import org.xhy.filter.FilterData;
import org.xhy.socket.codec.RpcResponse;

public interface ClientAfterFilter extends Filter<RpcResponse> {
}
