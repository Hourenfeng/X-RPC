package org.xhy.socket.serialization;

import java.io.IOException;

/**
 * 序列化接口
 */
public interface RpcSerialization {


    <T> byte[] serialize(T obj) throws IOException;

    <T> T deserialize(byte[] data, Class<T> clz) throws IOException;
}
