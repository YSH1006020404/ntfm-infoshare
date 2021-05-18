package cn.les.ntfm.infoshare.dao;

import cn.les.ntfm.infoshare.entity.LinkStatisticsDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 链路统计mapper
 *
 * @author 杨硕
 * @date 2020-07-08 8:59
 */
@Mapper
public interface LinkStatisticsMapper {
    /**
     * 新增
     *
     * @param linkStatisticsDO 链路统计信息
     */
    void addData(LinkStatisticsDO linkStatisticsDO);

    /**
     * 主键检索
     *
     * @param id 主键
     */
    LinkStatisticsDO getDataById(Long id);

    /**
     * 链路统计信息获取
     *
     * @param linkStatisticsDO 链路统计信息
     * @return list
     */
    List<LinkStatisticsDO> listData(LinkStatisticsDO linkStatisticsDO);

    /**
     * 主键删除
     *
     * @param id 主键
     */
    void deleteDataById(Long id);

    /**
     * 更新当天统计报文数量
     *
     * @param linkStatisticsDO 链路统计信息
     */
    void updateStatisticsCount(LinkStatisticsDO linkStatisticsDO);

    /**
     * 更新序列号
     *
     * @param linkStatisticsDO 链路统计信息
     */
    void updateSerialNumber(LinkStatisticsDO linkStatisticsDO);
}
