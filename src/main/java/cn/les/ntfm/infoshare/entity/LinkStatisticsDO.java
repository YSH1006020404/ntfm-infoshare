package cn.les.ntfm.infoshare.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 链路统计
 *
 * @author 杨硕
 * @date 2020-07-08 8:51
 */
@Setter
@Getter
@ToString
public class LinkStatisticsDO extends BaseEntity {
    /**
     * 链路主键
     */
    private Long infoshareconfigId;
    /**
     * 目的地主键
     */
    private Long destinationconfigId;
    /**
     * 统计时间（UTC时）
     */
    private String statisticsTime;
    /**
     * 当天统计报文数量
     */
    private Long statisticsCount;
    /**
     * 序列号
     */
    private Long serialNumber;
}
