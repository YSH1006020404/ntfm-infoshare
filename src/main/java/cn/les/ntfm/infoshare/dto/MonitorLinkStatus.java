package cn.les.ntfm.infoshare.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 链路监控详情
 *
 * @author 杨硕
 * @date 2020-05-03-上午11:42
 */
@Getter
@Setter
@ToString
public class MonitorLinkStatus implements Serializable {
    /**
     * 逻辑链路号
     */
    private Long logicNumber;
    /**
     * 开关状态
     */
    private Integer adminStatus;
    /**
     * 链路状态（信息共享模块判断链路状态一直为1，1正常 0 不正常）
     */
    private Integer linkStatus;
    /**
     * 发送数据量
     */
    private Integer sendCount;
}
