package cn.les.ntfm.infoshare.service.impl;

import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.entity.TcpipConfigDO;
import cn.les.ntfm.enums.SemaphoreOperationEnum;
import cn.les.ntfm.infoshare.dto.JobConfiguration;
import cn.les.ntfm.infoshare.service.AbstractSendTypeService;
import cn.les.ntfm.util.Log4jUtils;
import cn.les.ntfm.util.TcpConnectionUtils;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * TCPIP服务连接
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-23 16:12
 */
@Service(value = "tcpipSenderService")
public class TcpipSenderServiceImpl extends AbstractSendTypeService {
    @Override
    public void sendMsg(JobConfiguration jobConfiguration, String msg, Long destinationConfigId) throws InterruptedException {
        String logPath = jobConfiguration.getLinkID()
                + Constants.UNDERLINE + jobConfiguration.getInteractionMark()
                + Constants.UNDERLINE + jobConfiguration.getTableName()
                + File.separator + destinationConfigId
                + Constants.UNDERLINE + LogConstants.TCPIP
                + jobConfiguration.getDestinationConfigMaps().get(destinationConfigId).get(Constants.COLUMN_DESTINATION);
        synchronized (TcpipSenderServiceImpl.class) {
            // 获取发送对象
            if (Constants.tcpipUpdateTimeMap.get(destinationConfigId) == null
                    || jobConfiguration.getDestinationUpdateTime().get(destinationConfigId)
                    .compareTo(Constants.tcpipUpdateTimeMap.get(destinationConfigId)) != 0) {
                // 将tcpip的配置信息转换成实体类对象
                TcpipConfigDO tcpipConfigDO = JSON.parseObject(
                        JSON.toJSONString(jobConfiguration.getDestinationConfigMaps().get(destinationConfigId)), TcpipConfigDO.class);
                TcpConnectionUtils.doConnect(tcpipConfigDO, destinationConfigId, logPath);
            }
            //获取TCP连接许可
            TcpConnectionUtils.operateSemaphore(destinationConfigId, SemaphoreOperationEnum.ACQUIRE);
            Channel channel = Constants.tcpipChannelMap.get(destinationConfigId);
            channel.writeAndFlush(msg.getBytes())
                    .addListener((ChannelFutureListener) channelFuture -> {
                        //释放TCP连接许可
                        TcpConnectionUtils.operateSemaphore(destinationConfigId, SemaphoreOperationEnum.RELEASE);
                        if (channelFuture.isSuccess()) {
                            Log4jUtils.getInstance().getLogger(logPath + LogConstants.SEND_MSG)
                                    .warn("send msg to " + jobConfiguration.getDestinationConfigMaps().get(destinationConfigId).get(Constants.COLUMN_DESTINATION)
                                            + " " + msg.length() + "bytes :" + msg);
                        } else {
                            Log4jUtils.getInstance().getLogger(logPath + LogConstants.SEND_MSG)
                                    .error("failed to send msg to " + jobConfiguration.getDestinationConfigMaps().get(destinationConfigId).get(Constants.COLUMN_DESTINATION)
                                            + ":" + msg);
                        }
                    });
        }
    }
}