package cn.les.ntfm.infoshare.service.impl;

import cn.les.ntfm.infoshare.entity.TcpipConfigDO;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码器
 *
 * @author 杨硕
 * @date 2019-12-10 20:09
 */
public class TcpEncoder extends MessageToByteEncoder<byte[]> {
    private TcpipConfigDO tcpipConfigDO;

    public TcpEncoder(TcpipConfigDO tcpipConfigDO) {
        this.tcpipConfigDO = tcpipConfigDO;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, byte[] bytes, ByteBuf byteBuf) throws Exception {
        //添加消息头
        byteBuf.writeByte((byte) tcpipConfigDO.getNummsghead());
        //添加消息内容
        byteBuf.writeBytes(bytes);
        //添加消息尾
        byteBuf.writeByte((byte) tcpipConfigDO.getNummsgtail());
    }
}
