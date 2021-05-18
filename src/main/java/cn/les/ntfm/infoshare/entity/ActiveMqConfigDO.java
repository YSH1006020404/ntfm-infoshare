package cn.les.ntfm.infoshare.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * activeMq配置
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-03-13 11:51
 */
@Getter
@Setter
@ToString
public class ActiveMqConfigDO extends BaseEntity {
    /**
     * url
     */
    private String url;
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