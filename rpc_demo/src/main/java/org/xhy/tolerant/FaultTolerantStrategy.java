package org.xhy.tolerant;

import org.xhy.common.URL;

import java.util.List;

public interface FaultTolerantStrategy {

    Object handler(FaultContext faultContext) throws Exception;
}
