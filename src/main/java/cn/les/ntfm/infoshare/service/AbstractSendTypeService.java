package cn.les.ntfm.infoshare.service;

import cn.les.ntfm.infoshare.dto.JobConfiguration;

import javax.jms.JMSException;
import java.io.Serializable;

/**
 * 消息发送方式service
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-30 15:00
 */
public abstract class AbstractSendTypeService implements Serializable {

    /**
     * 发送数据
     *
     * @param jobConfiguration    发送目的地的配置信息
     * @param msg                 发送的消息
     * @param destinationConfigId 输出目的地id
     */
    public abstract void sendMsg(JobConfiguration jobConfiguration, String msg, Long destinationConfigId) throws Exception;
}
