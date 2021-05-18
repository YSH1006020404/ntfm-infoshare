package cn.les.ntfm.infoshare.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * tcpip配置
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-03-23 13:50
 */
@Getter
@Setter
@ToString
public class TcpipConfigDO extends BaseEntity {
    /**
     * IP地址
     */
    private String ips;
    /**
     * 端口号
     */
    private Integer port;

    /**
     * 头
     */
    private int nummsghead;
    /**
     * 尾
     */
    private int nummsgtail;
    /**
     * 对象名称
     */
    private String objectName;
    /**
     * 消息目的地关联信息
     */
    private DestinationConfigDO destinationConfigDO;

}
