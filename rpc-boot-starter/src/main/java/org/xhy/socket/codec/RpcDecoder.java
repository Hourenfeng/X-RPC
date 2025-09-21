package org.xhy.socket.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.xhy.common.constants.MsgType;
import org.xhy.common.constants.ProtocolConstants;
import org.xhy.config.Properties;
import org.xhy.socket.serialization.RpcSerialization;
import org.xhy.socket.serialization.SerializationFactory;


import java.util.*;

/**
 *解码器
 */
public class RpcDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {

        // 如果可读字节数少于协议头长度，说明还没有接收完整个协议头，直接返回
        if (in.readableBytes() < ProtocolConstants.HEADER_TOTAL_LEN) {
            return;
        }
        // 标记当前读取位置，便于后面回退
        in.markReaderIndex();

        // 读取魔数字段
        short magic = in.readShort();
        if (magic != ProtocolConstants.MAGIC) {
            throw new IllegalArgumentException("magic number is illegal, " + magic);
        }
        // 读取版本字段
        byte version = in.readByte();
        // 读取消息类型
        byte msgType = in.readByte();
        // 读取响应状态
        byte status = in.readByte();
        // 读取请求 ID
        long requestId = in.readLong();
        // 读取消息体长度
        int dataLength = in.readInt();
        // 如果可读字节数小于消息体长度，说明还没有接收完整个消息体，回退并返回
        if (in.readableBytes() < dataLength) {
            // 回退标记位置
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        // 读取数据
        in.readBytes(data);

        // 处理消息的类型
        MsgType msgTypeEnum = MsgType.findByType(msgType);
        if (msgTypeEnum == null) {
            return;
        }

        // 构建消息头
        MsgHeader header = new MsgHeader();
        header.setMagic(magic);
        header.setVersion(version);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setMsgType(msgType);
        header.setMsgLen(dataLength);

        RpcSerialization rpcSerialization = SerializationFactory.get(Properties.getSerialization());
        RpcProtocol protocol = new RpcProtocol<>();
        protocol.setHeader(header);
        switch (msgTypeEnum) {
            // 请求消息
            case REQUEST:
                RpcRequest request = rpcSerialization.deserialize(data, RpcRequest.class);
                protocol.setBody(request);
                break;
            // 响应消息
            case RESPONSE:
                RpcResponse response = rpcSerialization.deserialize(data, RpcResponse.class);
                protocol.setBody(response);
                break;
        }
        out.add(protocol);
    }


}
