package cn.les.ntfm.infoshare.service;

/**
 * 链路统计service
 *
 * @author 杨硕
 * @date 2020-07-08 9:19
 */
public interface LinkStatisticsService {
    /**
     * 获取最新的序列号
     *
     * @param infoshareconfigId 链路主键
     * @return String
     */
    String updateSerialNumber(Long infoshareconfigId);

    /**
     * 更新当天的报文数据量
     *
     * @param infoshareconfigId   链路主键
     * @param destinationconfigId 目的地主键
     */
    void updateStatisticsCount(Long infoshareconfigId, Long destinationconfigId);
}
