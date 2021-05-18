package cn.les.ntfm.infoshare.service.impl;

import cn.les.framework.core.util.SpringContextUtils;
import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.dto.JobConfiguration;
import cn.les.ntfm.infoshare.service.AbstractSendTypeService;
import cn.les.ntfm.util.Log4jUtils;

import java.io.File;
import java.util.Map;

/**
 * 数据发送线程
 *
 * @author 杨硕
 * @date 2020-03-19 15:57
 */
public class SendDataThread implements Runnable {
    private LinkStatisticsServiceImpl linkStatisticsService;
    private JobConfiguration jobConfiguration;
    private Map.Entry<Long, String> sendTypeServiceEntry;
    private String msg;

    public SendDataThread(Map.Entry<Long, String> sendTypeServiceEntry, JobConfiguration jobConfiguration, String msg) {
        this.sendTypeServiceEntry = sendTypeServiceEntry;
        this.jobConfiguration = jobConfiguration;
        this.msg = msg;
        if (linkStatisticsService == null) {
            linkStatisticsService = SpringContextUtils.getBean(LinkStatisticsServiceImpl.class);
        }
    }


    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        //TODO 文件名测试
        String logPath = jobConfiguration.getLinkID()
                + Constants.UNDERLINE + jobConfiguration.getInteractionMark()
                + Constants.UNDERLINE + jobConfiguration.getTableName()
                + File.separator + sendTypeServiceEntry.getValue().toLowerCase()
                + Constants.UNDERLINE + jobConfiguration.getDestinationConfigMaps().get(sendTypeServiceEntry.getKey()).get(Constants.COLUMN_DESTINATION);
        try {
            AbstractSendTypeService sendTypeService = (AbstractSendTypeService) SpringContextUtils.getBean(
                    Class.forName(Constants.SENDERCLASS_PATH
                            + sendTypeServiceEntry.getValue()
                            + Constants.SENDER_NAME_SUFFIX));
            sendTypeService.sendMsg(jobConfiguration, msg, sendTypeServiceEntry.getKey());

            //发送计数
            linkStatisticsService.updateStatisticsCount(jobConfiguration.getLinkID(), sendTypeServiceEntry.getKey());
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(logPath + LogConstants.ERROR_MSG)
                    .error("发送线程出现错误，错误原因：", e);
        }
    }

}