package cn.les.ntfm.infoshare.runner;

import cn.les.ntfm.constant.ConfigConstants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.constant.PropertyNameConstants;
import cn.les.ntfm.infoshare.entity.InfoshareConfigDO;
import cn.les.ntfm.infoshare.entity.PropertyDictDO;
import cn.les.ntfm.infoshare.dao.InfoShareConfigMapper;
import cn.les.ntfm.infoshare.dao.PropertyDictMapper;
import cn.les.ntfm.infoshare.quartz.scheduler.ScheduleManager;
import cn.les.ntfm.infoshare.service.JobConfigurationService;
import cn.les.ntfm.util.Log4jUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 数据传输定时任务
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-19 14:19
 */
@Component
@Order(value = 1)
public class DataTransferRunner implements ApplicationRunner {
    @Resource
    private JobConfigurationService jobConfigurationService;
    @Resource
    private ScheduleManager scheduleManager;
    @Resource
    private InfoShareConfigMapper infoShareConfigMapper;
    @Resource
    private PropertyDictMapper propertyDictMapper;
    @Autowired
    private ConfigConstants configConstants;

    @Override
    public void run(ApplicationArguments args) {
        try {
            //定时清除C_表
            scheduleManager.startCleanCtableJob();

            // 获取配置链路
            InfoshareConfigDO infoShareConfiguration = new InfoshareConfigDO();
            infoShareConfiguration.setStateFlag(true);
            List<InfoshareConfigDO> infoShareConfigurationList = infoShareConfigMapper.listInfoShareConfigurations(infoShareConfiguration);
            // 遍历所有链路，数据转发进线程池，数据抽取进定时任务
            if (infoShareConfigurationList != null && infoShareConfigurationList.size() > 0) {
            /*ExecutorService executorService = new ThreadPoolExecutor(configConstants.getCorePoolSize()
                    , configConstants.getMaximumPoolSize(),
                    0L
                    , TimeUnit.MILLISECONDS
                    , new LinkedBlockingQueue<>()
                    , new NamedThreadFactory(Constants.RECEIVE_POOL, "test"));*/
                //数据接收方式为数据库的字典项
                PropertyDictDO propertyDictDO = propertyDictMapper.getPropertyDictByTypeNameAndDictName(PropertyNameConstants.SOURCE_TYPE, PropertyNameConstants.DATABASE);
                if (propertyDictDO == null) {
                    Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                            .warn("没有数据接收方式为数据库的字典项！");
                    return;
                }
                for (InfoshareConfigDO configuration : infoShareConfigurationList) {
                    // 数据抽取的情况
                    if (propertyDictDO.getId().equals(configuration.getSourceType())) {
                        jobConfigurationService.doJob(configuration);
                    }
                    //TODO 数据转发的情况
                    /*else {
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                    }*/
                }

            }
            //发送链路状态给监控
            if (configConstants.getSendLinkStatus()) {
                scheduleManager.startSendLinkStatusJob();
            }
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("DataTransferRunner类的run方法出现错误，错误原因：" + e);
        }
    }


}