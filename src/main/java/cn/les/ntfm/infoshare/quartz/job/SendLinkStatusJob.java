package cn.les.ntfm.infoshare.quartz.job;

import cn.les.framework.zlmq.config.ZLConfig;
import cn.les.ntfm.constant.ConfigConstants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.dao.InfoShareConfigMapper;
import cn.les.ntfm.infoshare.dto.MonitorLinkStatus;
import cn.les.ntfm.infoshare.service.impl.LinkStatisticsServiceImpl;
import cn.les.ntfm.util.Log4jUtils;
import com.alibaba.fastjson.JSON;
import jni.ZLCallUtil;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 发送链路状态
 *
 * @author 杨硕
 * @date 2020-04-05-上午6:44
 */
@Component
@DisallowConcurrentExecution
public class SendLinkStatusJob implements Job {
    @Resource
    private InfoShareConfigMapper infoShareConfigMapper;
    @Resource
    private LinkStatisticsServiceImpl linkStatisticsService;
    @Autowired
    private ConfigConstants configConstants;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        try {
            List<MonitorLinkStatus> monitorLinkStatusList = infoShareConfigMapper
                    .listMonitorLinks(linkStatisticsService.getCurrentTime());
            ZLCallUtil.Xmsg_send30String_inThread(configConstants.getSendLinkStatusHeader(), JSON.toJSONString(monitorLinkStatusList), ZLConfig.getTno_index());
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .info("发送链路状态给监控：" + JSON.toJSONString(monitorLinkStatusList));
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("发送链路状态给监控出现错误，错误原因：" + e);
        }
    }
}
