package cn.les.ntfm.infoshare.quartz.job;

import cn.les.ntfm.constant.ConfigConstants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.dao.CommonMapper;
import cn.les.ntfm.util.Log4jUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 清除C_表（去重信息表）
 *
 * @author 杨硕
 * @date 2020-04-05-上午6:44
 */
@Component
@DisallowConcurrentExecution
public class CleanCtableJob implements Job {
    @Resource
    private CommonMapper commonMapper;

    @Resource(name = "configConstants")
    protected ConfigConstants configConstants;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        try {
            commonMapper.callProByName(configConstants.getCleanCtablePro());
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("CleanCtableJob的execute方法出现错误，错误原因：", e);
        }
    }
}
