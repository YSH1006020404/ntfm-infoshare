package cn.les.ntfm.infoshare.task;

import cn.les.framework.mybatis.dynamic.datasource.DynamicDataSource;
import cn.les.framework.mybatis.dynamic.datasource.DynamicDataSourceSwitcher;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.runner.DataTransferRunner;
import cn.les.ntfm.util.Log4jUtils;
import jni.ZLCallUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 全国流量一级中心主备集群数据库判断
 *
 * @author
 * @create 2020-10-27-下午8:05
 */
@Component
@ConditionalOnExpression("#{'ntfm'.equals(environment['infoshare.dataSourceLabel']) &&'dmz'.equals(T(cn.les.framework.zlmq.util.InitUtil).getREGION())}")
//@ConditionalOnExpression("#{'ntfm-center'.equals(environment['infoshare.dataSourceLabel'])}")
public class CheckNtfmCenterTask {
    @Autowired
    private DataTransferRunner dataTransferRunner;
    @Autowired
    private DynamicDataSourceSwitcher dynamicDataSourceSwitcher;

    /**
     * 根据主备集群状态判断数据库连接
     */
    @Scheduled(cron = "* * * * * *")
    public void checkDBStatus() {
        try {
            Integer masterSlaveFlag = ZLCallUtil.getNowSys_Zoneid();
            if (masterSlaveFlag == 1) {
                if ("slave".equals(DynamicDataSource.getDB_Type())) {
                    dynamicDataSourceSwitcher.switchDataSource("master");
                    dataTransferRunner.run(null);
                }
            } else if (masterSlaveFlag == 2) {
                if ("default".equals(DynamicDataSource.getDB_Type()) ||
                        "master".equals(DynamicDataSource.getDB_Type())) {
                    dynamicDataSourceSwitcher.switchDataSource("slave");
                    dataTransferRunner.run(null);
                }
            } else {
                Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                        .error("主备集群判断错误！");
            }
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("根据主备集群状态切换数据库出现错误，错误原因：" + e);
        }
    }
}
