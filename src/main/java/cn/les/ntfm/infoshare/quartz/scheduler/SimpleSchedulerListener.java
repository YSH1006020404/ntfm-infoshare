package cn.les.ntfm.infoshare.quartz.scheduler;

import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.entity.DestinationConfigDO;
import cn.les.ntfm.enums.SemaphoreOperationEnum;
import cn.les.ntfm.infoshare.dao.DestinationConfigRelationMapper;
import cn.les.ntfm.infoshare.service.impl.FtpMsgSender;
import cn.les.ntfm.util.Log4jUtils;
import cn.les.ntfm.util.TcpConnectionUtils;
import org.quartz.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 任务监听
 *
 * @author 杨硕
 * @create 2019-10-12 14:44
 */
@Component(value = "simpleSchedulerListener")
public class SimpleSchedulerListener implements SchedulerListener {
    @Resource
    private DestinationConfigRelationMapper destinationConfigRelationMapper;

    @Override
    public void jobScheduled(Trigger trigger) {

    }

    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {
        if (Constants.TRIGGER_KEY_CLEANCTABLE.equals(triggerKey.getName())
                || Constants.TRIGGER_KEY_SENDLINKSTATUS.equals(triggerKey.getName())
                || Constants.TRIGGER_KEY_CHECKDBSTATUS  .equals(triggerKey.getName())) {
            return;
        }
        DestinationConfigDO destinationParam = new DestinationConfigDO();
        destinationParam.setInfoshareconfigId(Long.parseLong(triggerKey.getGroup()));
        destinationParam.setStatusFlag(false);
        List<DestinationConfigDO> destinationConfigList = destinationConfigRelationMapper.listData(destinationParam);
        try {
            for (DestinationConfigDO destinationConfigDO : destinationConfigList) {
                // 停止activemq的相关线程
                if (Constants.activeMQConnectionMap.get(destinationConfigDO.getId()) != null) {
                    Constants.activeMQConnectionMap.get(destinationConfigDO.getId()).close();
                    Constants.activeMQConnectionMap.remove(destinationConfigDO.getId());
                }
                if (Constants.messageProducerMap.get(destinationConfigDO.getId()) != null) {
                    Constants.messageProducerMap.get(destinationConfigDO.getId()).close();
                    Constants.messageProducerMap.remove(destinationConfigDO.getId());
                }
                // 停止ibm的线程
                if (Constants.ibmMQConnectionMap.get(destinationConfigDO.getId()) != null) {
                    Constants.ibmMQConnectionMap.get(destinationConfigDO.getId()).destroy();
                    Constants.ibmMQConnectionMap.remove(destinationConfigDO.getId());
                }
                //TODO 停止FTP的相关线程
                FtpMsgSender ftpMsgSender = Constants.ftpMsgSenderMap.get(destinationConfigDO.getId());
                if (ftpMsgSender != null) {
                    ftpMsgSender.getExecutorService().shutdownNow();
                    if (ftpMsgSender.getFtpClient() != null && ftpMsgSender.getFtpClient().isConnected()) {
                        ftpMsgSender.getFtpClient().disconnect();
                    }
                    Constants.ftpMsgSenderMap.remove(destinationConfigDO.getId());
                }
                //TODO  停止TCPIP的相关线程
                if (Constants.tcpipChannelMap.get(destinationConfigDO.getId()) != null) {
//                    Constants.tcpipChannelMap.get(destinationConfigDO.getId()).close();
                    //释放TCP连接许可
                    TcpConnectionUtils.operateSemaphore(destinationConfigDO.getId(), SemaphoreOperationEnum.RELEASE);
                    Constants.tcpipChannelMap.remove(destinationConfigDO.getId());
                }
            }
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("SimpleSchedulerListener类的jobUnscheduled方法出现错误，错误原因：", e);
        }
    }

    @Override
    public void triggerFinalized(Trigger trigger) {

    }

    @Override
    public void triggerPaused(TriggerKey triggerKey) {

    }

    @Override
    public void triggersPaused(String s) {

    }

    @Override
    public void triggerResumed(TriggerKey triggerKey) {

    }

    @Override
    public void triggersResumed(String s) {

    }

    @Override
    public void jobAdded(JobDetail jobDetail) {

    }

    @Override
    public void jobDeleted(JobKey jobKey) {

    }

    @Override
    public void jobPaused(JobKey jobKey) {

    }

    @Override
    public void jobsPaused(String s) {

    }

    @Override
    public void jobResumed(JobKey jobKey) {

    }

    @Override
    public void jobsResumed(String s) {

    }

    @Override
    public void schedulerError(String s, SchedulerException e) {

    }

    @Override
    public void schedulerInStandbyMode() {

    }

    @Override
    public void schedulerStarted() {

    }

    @Override
    public void schedulerStarting() {

    }

    @Override
    public void schedulerShutdown() {
        System.out.println("schedulerShutdown：");
    }

    @Override
    public void schedulerShuttingdown() {
        System.out.println("schedulerShuttingdown");
    }

    @Override
    public void schedulingDataCleared() {

    }
}
