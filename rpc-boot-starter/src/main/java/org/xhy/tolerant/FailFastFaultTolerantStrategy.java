package org.xhy.tolerant;

/**
 *  快速失败
 */
public class FailFastFaultTolerantStrategy implements FaultTolerantStrategy{

    @Override
    public Object handler(FaultContext faultContext) throws Exception {
        throw faultContext.getException();
    }
}
