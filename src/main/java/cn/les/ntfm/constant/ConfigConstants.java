package cn.les.ntfm.constant;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 配置文件config.properties的数据
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-19 15:31
 */
@Component
@ConfigurationProperties(prefix = "infoshare")
@Getter
@Setter
@ToString
public class ConfigConstants {
    /**
     * 日志基础路径
     */
    public String logBasePath;
    /**
     * 服务器连接失败的确认周期
     */
    int comfirmPeriod;
    /**
     * 服务器连接检查周期
     */
    int checkDBPeriod;
    int checkActivePeriod;
    int checkIbmPeriod;
    int checkFtpPeriod;
    /**
     * 测试active连接时，等待返回值睡眠的时间
     */
    int activeSleepTime;
    /**
     * 判断实例存活时的参数
     */
    int missCheckPeriod;
    Boolean isOracleTest;
    Boolean isActiveTest;
    Boolean isIbmTest;
    Boolean isFtpTest;
    /**
     * 核心线程数
     */
    private int maximumPoolSize;
    /**
     * 最大线程数
     */
    private int corePoolSize;
    /**
     * 更新时间字段
     */
    private String updateTimeColumn;
    /**
     * 休眠时间（单位：毫秒）
     */
    private int sleepTime;
    /**
     * 数据丢弃的休眠时间（单位：毫秒）
     */
    private int discardSleepTime;
    /**
     * 数据发送的休眠时间（单位：毫秒）
     */
    private int sendMsgSleepTime;
    /**
     * 阻塞队列最大值
     */
    private int maxQueueSize;
    /**
     * freemarker模板路径
     */
    private String ftlPath;
    /**
     * 删除C_表的存储过程名称
     */
    private String cleanCtablePro;
    /**
     * 删除C_表的频率
     */
    private String cleanCtableProCron;
    /**
     * 数据源：ntfm-center(全国流量一级中心) ntfm-region(全国流量地区节点) atfm(华北流量或三地CDM)
     */
    private String dataSourceLabel;
    /**
     * 是否给监控发送链路状态信息
     */
    private Boolean sendLinkStatus;
    /**
     * 链路监控发送的消息频率
     */
    private String sendLinkStatusCron;
    /**
     * 判断主备集群状态的频率
     */
    private String checkDBStatusCron;
    /**
     * 链路监控发送的消息头
     */
    private String sendLinkStatusHeader;
    /**
     * 心跳消息发送频率
     */
    private String heartBeatFrequncy;


    /**********************checkConn start**********************************/
    /**
     * TCP消息最大长度
     */
    private int tcpMaxLength;
    /**
     * 日期字段的数据库存储类型：DATE/VARCHAR
     */
    private String dateType;
    /**
     * 日期类型数据由北京时转换为UTC时
     */
    private Boolean transferToUTC;
    /**
     * activemq服务的默认URL
     */
    private String defalutActviemqURL;
    /**
     * ibmmq服务的默认URL
     */
    private String defalutIbmmqURL;
    private String oracleDriver;
    private String oracleURL;
    private String oracleUserName;
    private String oraclePwd;
    private String activeURL;
    private String activeUserName;
    private String activePwd;
    private String ibmURL;
    private String ibmManager;
    private String channel;
    private String ftpURL;
    private String ftpUserName;
    private String ftpPwd;
    private int ftpPort;
    /************************checkConn end******************************************/
}