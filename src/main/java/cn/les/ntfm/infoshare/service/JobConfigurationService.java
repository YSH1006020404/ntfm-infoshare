package cn.les.ntfm.infoshare.service;

import cn.les.ntfm.infoshare.entity.InfoshareConfigDO;
import org.quartz.SchedulerException;

/**
 * job配置Service
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-29 15:16
 */
public interface JobConfigurationService {
    /**
     * job执行
     *
     * @param configuration 链路配置
     */
    void doJob(InfoshareConfigDO configuration);

    /**
     * 全量执行
     *
     * @param infoshareId 链路配置
     * @throws Exception 异常
     */
    void triggerAll(Long infoshareId) throws Exception;

    /**
     * 新增job
     *
     * @param linkId 链路ID
     */
    void addJob(Long linkId);


    /**
     * 刪除job
     *
     * @param linkId 链路ID
     * @throws SchedulerException 异常
     */
    void delJob(Long linkId);

}