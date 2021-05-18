package cn.les.ntfm.infoshare.quartz.scheduler;

import cn.les.ntfm.constant.ConfigConstants;
import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.dto.JobConfiguration;
import cn.les.ntfm.infoshare.quartz.job.BaseJob;
import cn.les.ntfm.infoshare.quartz.job.CleanCtableJob;
import cn.les.ntfm.infoshare.quartz.job.SendLinkStatusJob;
import cn.les.ntfm.util.Log4jUtils;
import org.quartz.*;
import org.quartz.impl.matchers.EverythingMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static org.quartz.CronScheduleBuilder.cronSchedule;

/**
 * 任务Service
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-03-14 14:02
 */
@Component(value = "scheduleManager")
public class ScheduleManager {
    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;
    @Resource(name = "configConstants")
    private ConfigConstants configConstants;
    @Resource
    private SimpleSchedulerListener simpleSchedulerListener;
    @Resource
    private SimpleJobListener simpleJobListener;
    private Scheduler scheduler;

    @PostConstruct
    private void init() {
        scheduler = schedulerFactoryBean.getScheduler();
        try {
            scheduler.getListenerManager().addSchedulerListener(simpleSchedulerListener);
            scheduler.getListenerManager().addJobListener(simpleJobListener, EverythingMatcher.allJobs());
        } catch (SchedulerException e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("ScheduleManager类的init方法出现错误，错误原因：", e);
        }
    }

    /**
     * 删除job
     *
     * @param job job配置
     */
    public void removeJob(JobConfiguration job) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());
        if (scheduler.checkExists(triggerKey)) {
            // 停止触发器
            scheduler.pauseTrigger(triggerKey);
            // 移除触发器
            scheduler.unscheduleJob(triggerKey);
            // 删除任务
            JobKey jobKey = JobKey.jobKey(job.getJobName(), job.getJobGroup());
            scheduler.deleteJob(jobKey);
        }
    }

    /**
     * 打开job
     *
     * @param jobConfiguration job配置信息
     * @throws Exception 异常
     */
    public void startJob(JobConfiguration jobConfiguration) throws Exception {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobConfiguration.getJobName(), jobConfiguration.getJobGroup());
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        if (trigger == null) {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(Constants.JOB_DATA_MAP, jobConfiguration);
            JobDetail jobDetail = JobBuilder.newJob(BaseJob.class)
                    .withIdentity(jobConfiguration.getJobName(), jobConfiguration.getJobGroup())
                    .setJobData(jobDataMap)
                    .build();
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(cronSchedule(jobConfiguration.getFrequency()))
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
        } else {
            scheduler.rescheduleJob(triggerKey, trigger);
        }
    }

    /**
     * 关闭job
     *
     * @param jobConfiguration job配置信息
     * @throws SchedulerException 异常
     */
    public void stopJob(JobConfiguration jobConfiguration) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobConfiguration.getJobName(), jobConfiguration.getJobGroup());
        scheduler.unscheduleJob(triggerKey);
    }

    /**
     * 清除C_表（去重信息表）job
     *
     * @throws Exception 异常
     */
    public void startCleanCtableJob() throws Exception {
        TriggerKey triggerKey = TriggerKey.triggerKey(Constants.TRIGGER_KEY_CLEANCTABLE);
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        if (trigger == null) {
            JobDetail jobDetail = JobBuilder.newJob(CleanCtableJob.class)
                    .withIdentity(Constants.TRIGGER_KEY_CLEANCTABLE)
                    .build();
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(cronSchedule(configConstants.getCleanCtableProCron()))
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
        } else {
            scheduler.rescheduleJob(triggerKey, trigger);
        }
    }

    /**
     * 发送链路状态给监控
     *
     * @throws Exception
     */
    public void startSendLinkStatusJob() throws Exception {
        TriggerKey triggerKey = TriggerKey.triggerKey(Constants.TRIGGER_KEY_SENDLINKSTATUS);
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        if (trigger == null) {
            JobDetail jobDetail = JobBuilder.newJob(SendLinkStatusJob.class)
                    .withIdentity(Constants.TRIGGER_KEY_SENDLINKSTATUS)
                    .build();
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(cronSchedule(configConstants.getSendLinkStatusCron()))
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
        } else {
            scheduler.rescheduleJob(triggerKey, trigger);
        }
    }

    public Scheduler getScheduler() {
        return this.scheduler;
    }

}