package cn.les.ntfm.infoshare.service.impl;

import cn.les.ntfm.infoshare.entity.TcpipConfigDO;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * 通道
 *
 * @author 杨硕
 * @date 2019-12-10 19:46
 */
public class TcpClientInitializer extends ChannelInitializer<SocketChannel> {
    private Bootstrap bootstrap;
    private TcpipConfigDO tcpipConfigDO;
    private Long destinationConfigId;
    private String logPath;

    public TcpClientInitializer(Bootstrap bootstrap, TcpipConfigDO tcpipConfigDO, Long destinationConfigId, String logPath) {
        this.bootstrap = bootstrap;
        this.tcpipConfigDO = tcpipConfigDO;
        this.destinationConfigId = destinationConfigId;
        this.logPath = logPath;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //编码
        pipeline.addLast(new TcpEncoder(tcpipConfigDO));
        //解码
        pipeline.addLast(new TcpDecoder(logPath, tcpipConfigDO));
        //数据处理
        pipeline.addLast(new TcpClientHandler(bootstrap, tcpipConfigDO, destinationConfigId, logPath));
    }
}
