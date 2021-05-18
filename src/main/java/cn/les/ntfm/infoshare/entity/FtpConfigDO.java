package cn.les.ntfm.infoshare.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ftp配置
 *
 * @author panyunpeng
 * @version 1.0
 * @date 2019-03-23 13:50
 */
@Getter
@Setter
@ToString
public class FtpConfigDO extends BaseEntity {
    /**
     * 服务器IP地址
     */
    private String ip;
    /**
     * 服务器端文件传输路径
     */
    private String path;
    /**
     * 服务器端口
     */
    private int port;
    /**
     * 文件名前缀
     */
    private String fileNamePrefix;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 对象名称
     */
    private String objectName;

    /**
     * 消息目的地关联信息
     */
    private DestinationConfigDO destinationConfigDO;

}
