package cn.les.ntfm.infoshare.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ibmMq配置
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-03-13 11:54
 */
@Getter
@Setter
@ToString
public class IbmMqConfigDO extends BaseEntity {
    /**
     * 地址
     */
    private String url;
    /**
     * 队列管理器
     */
    private String manager;

    /**
     * 通道
     */
    private String channel;
    /**
     * 编码
     */
    private Integer ccsid;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 密码
     */
    private String password;

    /**
     * 消息传递模式
     */
    private String processMode;
    /**
     * 目的地
     */
    private String destination;
    /**
     * 对象名称
     */
    private String objectName;
    /**
     * 消息目的地关联信息
     */
    private DestinationConfigDO destinationConfigDO;

}