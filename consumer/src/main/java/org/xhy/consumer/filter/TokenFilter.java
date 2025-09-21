package org.xhy.consumer.filter;

import org.xhy.filter.FilterData;
import org.xhy.filter.FilterResponse;
import org.xhy.filter.client.ClientBeforeFilter;
import org.xhy.socket.codec.RpcRequest;

public class TokenFilter implements ClientBeforeFilter {
    @Override
    public FilterResponse doFilter(FilterData<RpcRequest> filterData) {
        final RpcRequest rpcRequest = filterData.getObject();
        rpcRequest.getClientAttachments().put("token","xhy123");
        return new FilterResponse(true,null);
    }
}
