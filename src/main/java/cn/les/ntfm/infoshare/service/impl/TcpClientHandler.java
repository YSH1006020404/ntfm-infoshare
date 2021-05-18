package cn.les.ntfm.infoshare.service.impl;

import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.entity.TcpipConfigDO;
import cn.les.ntfm.enums.SemaphoreOperationEnum;
import cn.les.ntfm.util.Log4jUtils;
import cn.les.ntfm.util.TcpConnectionUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * tcp处理器
 *
 * @author 杨硕
 * @date 2019-12-11 8:45
 */
@ChannelHandler.Sharable
public class TcpClientHandler extends SimpleChannelInboundHandler<byte[]> {
    private Bootstrap bootstrap;
    private ExecutorService executorService;
    private TcpipConfigDO tcpipConfigDO;
    private Long destinationConfigId;
    private String logPath;
    private Logger errorLogger;

    public TcpClientHandler(Bootstrap bootstrap, TcpipConfigDO tcpipConfigDO, Long destinationConfigId, String logPath) {
        this.bootstrap = bootstrap;
        this.tcpipConfigDO = tcpipConfigDO;
        this.destinationConfigId = destinationConfigId;
        this.logPath = logPath;
        errorLogger = Log4jUtils.getInstance().getLogger(logPath + LogConstants.ERROR_MSG);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, byte[] bytes) throws Exception {
        //接收消息

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        errorLogger.error("处理异常，异常原因：", cause);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        //获取TCP连接许可
        Semaphore semaphore = Constants.tcpipSemaphoreMap.get(destinationConfigId);
        if (semaphore == null || semaphore.availablePermits() == Constants.SEMAPHORE_NUM) {
            TcpConnectionUtils.operateSemaphore(destinationConfigId, SemaphoreOperationEnum.ACQUIRE);
        }
        //重连
        ctx.channel().eventLoop().schedule(() -> {
            try {
                TcpConnectionUtils.reConnect(bootstrap, tcpipConfigDO, destinationConfigId, logPath);
            } catch (InterruptedException e) {
                errorLogger.error("TcpClientHandler类的channelUnregistered方法出现错误，错误原因：", e);
            }
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Constants.tcpipUpdateTimeMap.put(destinationConfigId, tcpipConfigDO.getUpdateTime());
        Constants.tcpipChannelMap.put(destinationConfigId, ctx.channel());
        //释放TCP连接许可
        TcpConnectionUtils.operateSemaphore(destinationConfigId, SemaphoreOperationEnum.RELEASE);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        //关闭发送线程
        if (executorService != null) {
            executorService.shutdownNow();
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        }
        errorLogger.error("TCPIP" + ctx.channel().remoteAddress() + "连接断开!正在尝试重连操作...");
    }
}
