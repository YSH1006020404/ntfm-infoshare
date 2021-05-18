package cn.les.ntfm.util;

import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.entity.TcpipConfigDO;
import cn.les.ntfm.enums.SemaphoreOperationEnum;
import cn.les.ntfm.infoshare.service.impl.TcpClientInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.Semaphore;

/**
 * tcp连接工具类
 *
 * @author 杨硕
 * @date 2019-12-11 15:50
 */
public class TcpConnectionUtils {
    /**
     * TCP连接
     *
     * @param tcpipConfigDO       连接信息
     * @param destinationConfigId 输出目的地
     * @param logPath             日志路径
     * @throws InterruptedException 异常
     */
    public static void doConnect(TcpipConfigDO tcpipConfigDO, Long destinationConfigId, String logPath) throws InterruptedException {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new TcpClientInitializer(bootstrap, tcpipConfigDO, destinationConfigId, logPath));
        reConnect(bootstrap, tcpipConfigDO, destinationConfigId, logPath);
    }

    /**
     * TCP重连
     *
     * @param bootstrap           bootstrap
     * @param tcpipConfigDO       连接信息
     * @param destinationConfigId 输出目的地
     * @param logPath             日志路径
     */
    public static synchronized void reConnect(Bootstrap bootstrap, TcpipConfigDO tcpipConfigDO, Long destinationConfigId, String logPath) throws InterruptedException {
        Integer index = Constants.tcpipIndexMap.get(destinationConfigId);
        String[] ips = tcpipConfigDO.getIps().split(Constants.COMMA);
        if (index == null) {
            index = 0;
        } else {
            index = (++index) % ips.length;
        }
        Constants.tcpipIndexMap.put(destinationConfigId, index);
        if (Constants.tcpipChannelMap.get(destinationConfigId) == null
                || !Constants.tcpipChannelMap.get(destinationConfigId).isActive()) {
            bootstrap.connect(
                    ips[index]
                    , tcpipConfigDO.getPort()).sync()
                    .addListener((GenericFutureListener<ChannelFuture>) channelFuture -> {
                        if (channelFuture.isSuccess()) {
                            Log4jUtils.getInstance().getLogger(logPath + LogConstants.ERROR_MSG)
                                    .warn("tcp连接成功!" + channelFuture.channel().remoteAddress());
                        } else {
                            //获取TCP连接许可
                            TcpConnectionUtils.operateSemaphore(destinationConfigId, SemaphoreOperationEnum.ACQUIRE);
                            Log4jUtils.getInstance().getLogger(logPath + LogConstants.ERROR_MSG)
                                    .warn("tcp连接失败!" + channelFuture.channel().remoteAddress());
                        }
                    });
        }
    }

    /***
     * 操作TCP的连接许可
     * @param source 数据链路
     * @param semaphoreOperation 操作类型
     * @throws InterruptedException 异常
     */
    public static void operateSemaphore(Long source, SemaphoreOperationEnum semaphoreOperation) throws
            InterruptedException {
        Semaphore semaphore = Constants.tcpipSemaphoreMap.get(source);
        if (semaphore == null) {
            Semaphore semaphoreTmp = new Semaphore(Constants.SEMAPHORE_NUM);
            //获取许可
            if (SemaphoreOperationEnum.ACQUIRE.equals(semaphoreOperation)) {
                semaphoreTmp.acquire();
            }
            Constants.tcpipSemaphoreMap.put(source, semaphoreTmp);
        } else {
            if (SemaphoreOperationEnum.ACQUIRE.equals(semaphoreOperation)) {
                //获取许可
                semaphore.acquire();
            } else {
                //释放许可
                semaphore.release();
            }
        }
    }
}
