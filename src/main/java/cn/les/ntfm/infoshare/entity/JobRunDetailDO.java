package cn.les.ntfm.infoshare.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * job执行状况
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-24 16:07
 */
@Setter
@Getter
@ToString
public class JobRunDetailDO {
    /**
     * 主键
     */
    private Long id;
    /**
     * JOB组
     */
    private String jobName;
    /**
     * JOB名称
     */
    private String jobGroup;
    /**
     * 已发送增量数据的最新时间
     */
    private Date checkTime;
}