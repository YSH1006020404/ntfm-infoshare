package cn.les.ntfm.infoshare.task;

import cn.les.framework.mybatis.dynamic.datasource.DynamicDataSource;
import cn.les.framework.mybatis.dynamic.datasource.DynamicDataSourceSwitcher;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.util.Log4jUtils;
import jni.ZLCallUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * TODO 华北流量或三地CDM主备数据库判断
 *
 * @author
 * @create 2020-10-27-下午8:05
 */
@Component
@ConditionalOnExpression("#{'atfm'.equals(environment['infoshare.dataSourceLabel'])}")
public class CheckAtfmTask {
    @Autowired
    private DynamicDataSourceSwitcher dynamicDataSourceSwitcher;

    /**
     * 根据主备集群状态判断数据库连接
     */
    @Scheduled(cron = "* * * * * *")
    public void checkDBStatus() {
        try {
            byte[] masterSlave = ZLCallUtil.get_status(3);
            if (masterSlave[0] == 1) {
                if ("slave".equals(DynamicDataSource.getDB_Type())) {
                    dynamicDataSourceSwitcher.switchDataSource("master");
                }
            } else if (masterSlave[0] == 2) {
                if ("default".equals(DynamicDataSource.getDB_Type()) ||
                        "master".equals(DynamicDataSource.getDB_Type())) {
                    dynamicDataSourceSwitcher.switchDataSource("slave");
                }
            } else {
                Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                        .error("主备数据库判断错误！");
            }
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("根据主备数据库状态切换数据库出现错误，错误原因：" + e);
        }
    }
}
