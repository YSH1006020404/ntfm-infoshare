package cn.les.ntfm.infoshare.service.impl;

import cn.les.ntfm.constant.ConfigConstants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.entity.TcpipConfigDO;
import cn.les.ntfm.util.Log4jUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.List;

/**
 * 解码器
 *
 * @author 杨硕
 * @date 2019-12-10 20:10
 */
public class TcpDecoder extends ByteToMessageDecoder {
    @Resource
    private ConfigConstants configConstants;
    private TcpipConfigDO tcpipConfigDO;
    private Logger errorLogger;

    public TcpDecoder(String logPath, TcpipConfigDO tcpipConfigDO) {
        this.tcpipConfigDO = tcpipConfigDO;
        errorLogger = Log4jUtils.getInstance().getLogger(logPath + LogConstants.SEND_MSG);
    }


    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int length = byteBuf.readableBytes();
        //消息头格式和消息长度验证
        if (!(isValidLength(length) && isValidHead(byteBuf))) {
            channelHandlerContext.channel().close();
            return;
        }
        //获取消息尾索引
        Integer tailIndex = getTailIndex(byteBuf, length);
        if (tailIndex == null) {
            //消息尾格式验证
            channelHandlerContext.channel().close();
        } else if (tailIndex == 0) {
            //处理拆包情况
            return;
        } else {
            //去掉消息头
            byteBuf.readByte();
            //读取消息内容
            byte[] result = new byte[tailIndex - 1];
            byteBuf.readBytes(result, 0, tailIndex - 1);
            list.add(result);
            //去掉消息尾
            byteBuf.readByte();
        }
    }

    /**
     * 消息头格式验证
     *
     * @param byteBuf 消息内容
     * @return Boolean
     */
    private Boolean isValidHead(ByteBuf byteBuf) {
        byte head = byteBuf.getByte(byteBuf.readerIndex());
        if ((byte) tcpipConfigDO.getNummsghead() != head) {
            errorLogger.error("数据格式错误：消息头不正确！" + ArrayUtils.toString(byteBuf));
            return false;
        } else {
            return true;
        }
    }

    private Integer getTailIndex(ByteBuf byteBuf, int length) {
        int tailIndex = 0;
        byte content;
        for (int i = 0; i < length; i++) {
            content = byteBuf.getByte(byteBuf.readerIndex() + i);
            if (i > 0 && (byte) tcpipConfigDO.getNummsghead() == content) {
                errorLogger.error("数据格式错误：没有消息尾！" + ArrayUtils.toString(byteBuf));
                return null;
            } else if ((byte) tcpipConfigDO.getNummsgtail() == content) {
                tailIndex = i;
                break;
            }
        }
        return tailIndex;
    }

    /**
     * 长度验证
     *
     * @param length TCP消息最大长度
     * @return Boolean
     */
    private Boolean isValidLength(int length) {
        if (length > configConstants.getTcpMaxLength()) {
            errorLogger.error("数据超长！控制在" + configConstants.getTcpMaxLength() + "个字节以内!");
            return false;
        } else {
            return true;
        }
    }
}
