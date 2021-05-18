package cn.les.ntfm.infoshare.quartz.scheduler;

import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.entity.JobRunDetailDO;
import cn.les.ntfm.infoshare.dao.JobRunDetailMapper;
import cn.les.ntfm.infoshare.dto.JobConfiguration;
import cn.les.ntfm.util.Log4jUtils;
import com.alibaba.fastjson.JSON;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 任务监听
 *
 * @author 罗阳
 * @create 2019-11-26 16:38
 */
@Component(value = "simpleJobListener")
public class SimpleJobListener implements JobListener {

    @Resource
    private JobRunDetailMapper jobRunDetailMapper;

    @Override
    public String getName() {
        return "simpleJobListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {

    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {

    }

    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
        if (Constants.TRIGGER_KEY_CLEANCTABLE.equals(jobExecutionContext.getTrigger().getKey().getName())
                || Constants.TRIGGER_KEY_SENDLINKSTATUS.equals(jobExecutionContext.getTrigger().getKey().getName())
                || Constants.TRIGGER_KEY_CHECKDBSTATUS.equals(jobExecutionContext.getTrigger().getKey().getName())
                || Constants.SEND_TYPE_HEARTBEAT.equals(jobExecutionContext.getTrigger().getKey().getName())) {
            return;
        }
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        try {
            Object jobConfigurationObject = jobDataMap.get(Constants.JOB_DATA_MAP);
            JobConfiguration jobConfiguration = JSON.parseObject(JSON.toJSON(jobConfigurationObject).toString(), JobConfiguration.class);
            JobRunDetailDO jobRunDetail = jobConfiguration.getJobRunDetail();
            if (jobRunDetail != null) {
                jobRunDetailMapper.updateJobRunDetailById(jobRunDetail);
            }
        } catch (Exception ex) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("SimpleJobListener类的jobWasExecuted方法（任务结束时对时间更新）出现错误，错误原因：", ex);
        }

    }
}
