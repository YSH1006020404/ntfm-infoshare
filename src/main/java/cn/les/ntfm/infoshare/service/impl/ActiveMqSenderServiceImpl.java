package cn.les.ntfm.infoshare.service.impl;

import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.entity.ActiveMqConfigDO;
import cn.les.ntfm.infoshare.dto.JobConfiguration;
import cn.les.ntfm.infoshare.service.AbstractSendTypeService;
import cn.les.ntfm.util.Log4jUtils;
import com.alibaba.fastjson.JSON;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQMessageProducer;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.io.File;

/**
 * activemq服务连接
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-23 16:12
 */
@Service(value = "activeMqSenderService")
public class ActiveMqSenderServiceImpl extends AbstractSendTypeService {
    private ActiveMQMessageProducer activeMQMessageProducer;
    private BytesMessage bytesMessage;

    @Override
    public void sendMsg(JobConfiguration jobConfiguration, String msg, Long destinationConfigId) throws Exception {
        String logPath = jobConfiguration.getLinkID()
                + Constants.UNDERLINE + jobConfiguration.getInteractionMark()
                + Constants.UNDERLINE + jobConfiguration.getTableName()
                + File.separator + destinationConfigId
                + Constants.UNDERLINE + LogConstants.ACTIVEMQ
                + jobConfiguration.getDestinationConfigMaps().get(destinationConfigId).get(Constants.COLUMN_DESTINATION);
        // 将activemq的配置信息转换成实体类对象
        ActiveMqConfigDO activeMqConfiguration = JSON.parseObject(
                JSON.toJSONString(jobConfiguration.getDestinationConfigMaps().get(destinationConfigId)), ActiveMqConfigDO.class);
        // 发送数据
        //TODO 数据发送频繁，效率降低
        bytesMessage.writeBytes(msg.getBytes());
        activeMQMessageProducer.send(bytesMessage);
        Log4jUtils.getInstance().getLogger(logPath + LogConstants.SEND_MSG)
                .warn("send msg to " + activeMqConfiguration.getUrl() + Constants.UNDERLINE + activeMqConfiguration.getObjectName()
                        + " " + msg.length() + "bytes :" + msg);
    }


    /**
     * 获取JmsTemplate
     *
     * @param activeMqConfiguration 配置信息
     * @return JmsTemplate
     */
    private void getMessageProducer(ActiveMqConfigDO activeMqConfiguration, Long key) throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                activeMqConfiguration.getUrl());
        ActiveMQConnection connection = (ActiveMQConnection) connectionFactory.createConnection(
                activeMqConfiguration.getUserName()
                , activeMqConfiguration.getPassword()
        );
        Constants.activeMQConnectionMap.put(key, connection);
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination;
        if (Constants.PROCESS_MODE_QUEUE.equals(activeMqConfiguration.getProcessMode())) {
            destination = new ActiveMQQueue(activeMqConfiguration.getDestination());
        } else {
            destination = new ActiveMQTopic(activeMqConfiguration.getDestination());
        }
        activeMQMessageProducer = (ActiveMQMessageProducer) session.createProducer(destination);
        bytesMessage = session.createBytesMessage();
    }

}