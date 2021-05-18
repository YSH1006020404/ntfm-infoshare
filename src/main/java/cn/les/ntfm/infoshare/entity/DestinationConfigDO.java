package cn.les.ntfm.infoshare.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 消息目的地关联
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-20 14:13
 */
@Getter
@Setter
@ToString
public class DestinationConfigDO {
    /**
     * 主键
     */
    private Long id;
    /**
     * 信息共享链路配置主键
     */
    private Long infoshareconfigId;
    /**
     * 消息目的地类型的字典项主键
     */
    private Long destinationTypeId;
    /**
     * 消息目的地配置表名
     */
    private String destinationType;
    /**
     * 消息目的地关联配置的主键
     */
    private Long destinationId;
    /**
     * 状态（0：关闭 1：开启）
     */
    private Boolean statusFlag;

}