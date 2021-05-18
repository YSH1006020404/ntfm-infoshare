package cn.les.ntfm.infoshare.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 信息共享配置
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-03-12 14:48
 */
@Getter
@Setter
@ToString
public class InfoshareConfigDO extends BaseEntity {
    /**
     * 链路描述
     */
    private String linkDescription;
    /**
     * 交互标识
     */
    private String interactionMark;
    /**
     * 单位名称
     */
    private String companyName;
    /**
     * 开关状态(0:关闭 1:开启)
     */
    private Boolean stateFlag;
    /**
     * 交互类型(IN:输入 OUT:输出)
     */
    private String interactionType;
    /**
     * 数据接收方式
     */
    private Long sourceType;
    /**
     * 数据接收配置关联主键
     */
    private Long sourceId;
}