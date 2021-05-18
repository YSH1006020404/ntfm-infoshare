package cn.les.ntfm.infoshare.service.impl;

import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.entity.IbmMqConfigDO;
import cn.les.ntfm.infoshare.dto.JobConfiguration;
import cn.les.ntfm.infoshare.service.AbstractSendTypeService;
import cn.les.ntfm.util.Log4jUtils;
import com.alibaba.fastjson.JSON;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQTopic;
import com.ibm.msg.client.wmq.WMQConstants;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.Destination;
import javax.jms.JMSException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service(value = "ibmMqSenderService")
public class IbmMqSenderServiceImpl extends AbstractSendTypeService {

    @Override
    public void sendMsg(JobConfiguration jobConfiguration, String msg, Long destinationConfigId) throws JMSException {
        String logPath = jobConfiguration.getLinkID()
                + Constants.UNDERLINE + jobConfiguration.getInteractionMark()
                + Constants.UNDERLINE + jobConfiguration.getTableName()
                + File.separator + destinationConfigId
                + Constants.UNDERLINE + LogConstants.IBMMQ
                + jobConfiguration.getDestinationConfigMaps().get(destinationConfigId).get(Constants.COLUMN_DESTINATION);
        // 将ibmmq的配置信息转换成实体类对象
        IbmMqConfigDO ibmMqConfiguration = JSON.parseObject(
                JSON.toJSONString(jobConfiguration.getDestinationConfigMaps().get(destinationConfigId)), IbmMqConfigDO.class);
        // 获取发送对象
        JmsTemplate jmsTemplate;
        if (Constants.ibmmqUpdateTimeMap.get(destinationConfigId) != null
                && jobConfiguration.getDestinationUpdateTime().get(destinationConfigId)
                .compareTo(Constants.ibmmqUpdateTimeMap.get(destinationConfigId)) == 0) {
            jmsTemplate = Constants.jmsTemplateMap.get(destinationConfigId);
        } else {
            Map<String, Object> jmsTemplateMap = getJmsTemplate(ibmMqConfiguration);
            jmsTemplate = (JmsTemplate) jmsTemplateMap.get("jmsTemplate");
            Constants.ibmmqUpdateTimeMap.put(destinationConfigId, ibmMqConfiguration.getUpdateTime());
            Constants.jmsTemplateMap.put(destinationConfigId, jmsTemplate);
            Constants.ibmMQConnectionMap.put(destinationConfigId,
                    (CachingConnectionFactory) jmsTemplateMap.get("cachingConnectionFactory"));
        }

        //TODO 发送数据
        jmsTemplate.convertAndSend(msg.getBytes());
        Log4jUtils.getInstance().getLogger(logPath + LogConstants.SEND_MSG)
                .warn("send msg to " + ibmMqConfiguration.getUrl() + Constants.UNDERLINE + ibmMqConfiguration.getObjectName()
                        + " " + msg.length() + "bytes :" + msg);
    }


    private Map<String, Object> getJmsTemplate(IbmMqConfigDO ibmMqConfiguration) throws JMSException {
        Map<String, Object> map = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
        MQConnectionFactory connectionFactory = new MQConnectionFactory();
        connectionFactory.setConnectionNameList(ibmMqConfiguration.getUrl());
        connectionFactory.setQueueManager(ibmMqConfiguration.getManager());
        connectionFactory.setChannel(ibmMqConfiguration.getChannel());
        connectionFactory.setCCSID(ibmMqConfiguration.getCcsid());
        //将传送类型设置为下列选项之一 （当 MQSeries 服务器和客户机不在同一主机上时使用）
        connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
        UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter = new UserCredentialsConnectionFactoryAdapter();
        userCredentialsConnectionFactoryAdapter.setUsername(ibmMqConfiguration.getUserName());
        userCredentialsConnectionFactoryAdapter.setPassword(ibmMqConfiguration.getPassword());
        userCredentialsConnectionFactoryAdapter.setTargetConnectionFactory(connectionFactory);
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setSessionCacheSize(500);
        cachingConnectionFactory.setReconnectOnException(true);
        cachingConnectionFactory.setTargetConnectionFactory(userCredentialsConnectionFactoryAdapter);
        Destination destination;
        if (Constants.PROCESS_MODE_QUEUE.equals(ibmMqConfiguration.getProcessMode())) {
            destination = new MQQueue(ibmMqConfiguration.getDestination());
        } else {
            destination = new MQTopic(ibmMqConfiguration.getDestination());
        }
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(cachingConnectionFactory);
        jmsTemplate.setDefaultDestination(destination);
        jmsTemplate.setPubSubDomain(false);
        map.put("jmsTemplate", jmsTemplate);
        map.put("cachingConnectionFactory", cachingConnectionFactory);
        return map;
    }
}
