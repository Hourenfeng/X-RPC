package org.xhy.event;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RpcListerLoader {

    private static ExecutorService eventThreadPool = Executors.newFixedThreadPool(2);


    private static List<IRpcLister> rpcListerList = new ArrayList<>();

    public void init(){
        // 轮询3个监听器，因为有三种事件
        registerLister(new AddRpcLister());
        registerLister(new DestroyRpcLister());
        registerLister(new UpdateRpcLister());
    }
    public static void registerLister(IRpcLister rpcLister){
        rpcListerList.add(rpcLister);
    }

    public static void sendEvent(RpcEventData eventData){
        if (eventData == null){
            return;
        }
        if (!rpcListerList.isEmpty()){
            for (IRpcLister iRpcLister : rpcListerList) {
                // 获取接口上的泛型，看上游传进来的eventData是哪一种监听器，去匹配
                final Class<?> generics = getInterfaceGenerics(iRpcLister);
                if (eventData.getClass().equals(generics)){
                    eventThreadPool.execute(()->{
                        iRpcLister.exec(eventData);
                    });
                }
            }
        }

    }

    public static Class<?> getInterfaceGenerics(Object o) {
        Type[] types = o.getClass().getGenericInterfaces();
        ParameterizedType parameterizedType = (ParameterizedType) types[0];
        Type type = parameterizedType.getActualTypeArguments()[0];
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        return null;
    }
}
