package cn.les.ntfm.infoshare.service.impl;

import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.entity.FtpConfigDO;
import cn.les.ntfm.infoshare.dto.JobConfiguration;
import cn.les.ntfm.infoshare.service.AbstractSendTypeService;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * TCPIP服务连接
 *
 * @author panyunpeng
 * @version 1.0
 * @date 2019-04-23 16:12
 */
@Service(value = "ftpSenderService")
public class FtpSenderServiceImpl extends AbstractSendTypeService {
    @Override
    public void sendMsg(JobConfiguration jobConfiguration, String msg, Long destinationConfigId) {
        String logPath = jobConfiguration.getLinkID()
                + Constants.UNDERLINE + jobConfiguration.getInteractionMark()
                + Constants.UNDERLINE + jobConfiguration.getTableName()
                + File.separator + destinationConfigId
                + Constants.UNDERLINE + LogConstants.FTP
                + jobConfiguration.getDestinationConfigMaps().get(destinationConfigId).get(Constants.COLUMN_DESTINATION);

        //获取发送对象
        FtpMsgSender ftpMsgSender;
        if (Constants.ftpUpdateTimeMap.get(destinationConfigId) != null
                && jobConfiguration.getDestinationUpdateTime().get(destinationConfigId)
                .compareTo(Constants.ftpUpdateTimeMap.get(destinationConfigId)) == 0) {
            ftpMsgSender = Constants.ftpMsgSenderMap.get(destinationConfigId);
        } else {
            FtpConfigDO ftpConfigDO = JSON.parseObject(
                    JSON.toJSONString(jobConfiguration.getDestinationConfigMaps().get(destinationConfigId)), FtpConfigDO.class);
            ftpMsgSender = new FtpMsgSender(ftpConfigDO, destinationConfigId);
            Constants.ftpUpdateTimeMap.put(destinationConfigId, ftpConfigDO.getUpdateTime());
            Constants.ftpMsgSenderMap.put(destinationConfigId, ftpMsgSender);
        }

        // 发送数据
        ftpMsgSender.sendMsg(msg, logPath);
    }
}