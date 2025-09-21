package org.xhy.filter.server;

import org.xhy.filter.Filter;
import org.xhy.socket.codec.RpcResponse;

public interface ServerAfterFilter extends Filter<RpcResponse> {
}
