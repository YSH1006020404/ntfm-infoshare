package cn.les.ntfm.constant;

import cn.les.ntfm.infoshare.service.impl.FtpMsgSender;
import cn.les.ntfm.util.FtpUtil;
import freemarker.template.Template;
import io.netty.channel.Channel;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQMessageProducer;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * 常量
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-03-13 16:28
 */
public class Constants {
    /**
     * 任务调度器的状态
     */
    public static final String PAUSE = "pause";
    /**
     * job参数
     */
    public static final String JOB_DATA_MAP = "jobData";
    /**
     * 清除C—表
     */
    public static final String TRIGGER_KEY_CLEANCTABLE = "cleanCtable";
    /**
     * 发送链路状态给监控
     */
    public static final String TRIGGER_KEY_SENDLINKSTATUS = "sendLinkStatus";
    /**
     * 根据主备集群状态判断数据库连接
     */
    public static final String TRIGGER_KEY_CHECKDBSTATUS = "checkDBStatus";
    /**
     * 消息发送方式类路径
     */
    public static final String SENDERCLASS_PATH = "cn.les.ntfm.infoshare.service.impl.";
    /**
     * 消息输出的发送service名称后缀
     */
    public static final String SENDER_NAME_SUFFIX = "SenderServiceImpl";
    /**
     * ftl文件名后缀
     */
    public static final String FILE_FTL_SUFFIX = ".ftl";

    /**
     * SQL关键字
     */
    public static final String SQL_SELECT = "SELECT ";
    public static final String SQL_SELECT_CONSTANT = "SELECT COUNT(1) FROM ";
    public static final String SQL_AS = " AS ";
    public static final String SQL_FROM = " FROM ";
    public static final String SQL_MERGE_INTO = "MERGE INTO ";
    public static final String SQL_USING = " USING ";
    public static final String SQL_LEFT_JOIN = " LEFT JOIN ";
    public static final String SQL_ON = " ON ";
    public static final String SQL_MATCHED = " WHEN MATCHED THEN ";
    public static final String SQL_NOT_MATCHED = " WHEN NOT MATCHED THEN ";
    public static final String SQL_UPDATE = " UPDATE ";
    public static final String SQL_SET = " SET ";
    public static final String SQL_EQUALS = " = ";
    public static final String SQL_WHERE = " WHERE ";
    public static final String SQL_INSERT = " INSERT ";
    public static final String SQL_VALUES = " VALUES ";
    public static final String SQL_TO_CHAR = " TO_CHAR ";
    public static final String SQL_TO_DATE = " TO_DATE ";
    public static final String SQL_NVL = " NVL ";
    public static final String SQL_OR = " OR ";
    public static final String SQL_AND = " AND ";
    public static final String SQL_ORDER_BY = " ORDER BY ";
    public static final String SQL_ORDER_ASC = " ASC ";
    public static final String SQL_ORDER_DESC = " DESC ";
    public static final String SQL_TO_UTC = " - INTERVAL '8'  HOUR ";
    public static final String SQL_DBMS_LOB = "DBMS_LOB.SUBSTR";
    /**
     * 数据库特殊值
     */
    public static final String SQL_TABLE_C_PREFIX = "C_";
    public static final String SQL_UPDATE_TIME_COLUMN = "MY_UPDATE_TIME";
    public static final String SQL_UPDATE_TIME_COLUMN_RESULT = "myUpdateTime";
    public static final String SQL_UPDATE_TIME_PARAM = "#{time}";
    public static final String SQL_RELATION_COLUMN_PARAM = "relationValue";
    public static final String YYYYMMDDHH24MISS = "'yyyymmddhh24miss'";
    public static final String YYYYMMDDHH24MISSSSS = "'yyyymmddhh24missSSS'";
    public static final String SQL_LINKID = "LINKID";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DESTINATION = "objectName";
    /**
     * 各种符号
     */
    public static final String COMMA = ",";
    public static final String DOT = ".";
    public static final String UNDERLINE = "_";
    public static final String SLASH = "/";
    public static final String SINGLE_QUOTES = "'";
    public static final String SQL_GREATER_THAN_SIGN = ">";
    public static final String MINUS = " - ";
    public static final String SQL_LESS_THAN_SIGN = "<";
    public static final String SQL_HASH = "#";
    public static final String SQL_OPEN_BRACE = "{";
    public static final String SQL_CLOSE_BRACE = "}";
    public static final String SQL_OPEN_PARENTHESIS = "(";
    public static final String SQL_CLOSE_PARENTHESIS = ")";
    /**
     * 数字
     */
    public static final Integer HASHMAP_INITIAL_CAPACITY = 16;
    /**
     * 时间格式
     */
    public static final String DATE_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    public static final String DATE_YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";
    public static final String DATE_YYYYMMDD = "yyyyMMdd";
    public static final String DATE_YYYYMMDDHH = "yyyyMMddHH";
    /**
     * 线程池名称
     */
    public static final String RECEIVE_POOL = "receive_pool";
    public static final String SEND_POOL = "send_pool";

    /***************************xml标签的数据类型Enum-start***************************/
    /**
     * 文本
     */
    public static final String XML_COLUMN_TYPE_TXT = "TXT";
    /**
     * 消息子类型
     */
    public static final String XML_COLUMN_TYPE_SUBTYPE = "SUBTYPE";
    /**
     * 时间年月日时分秒
     */
    public static final String XML_COLUMN_TYPE_YYYYMMDDHHMMSS = "YYYYMMDDHHMMSS";
    /**
     * 流水号
     */
    public static final String XML_COLUMN_TYPE_SERIAL = "SERIAL";
    /**
     * 报文数据量统计：全量/增量(ALL/INC)_一次任务报文数据量_报文当前页数/报文总页数_当前报文数据量
     */
    public static final String XML_COLUMN_TYPE_STATISTICS = "STATISTICS";
    /**
     * LIST标签（基础表）
     */
    public static final String XML_COLUMN_TYPE_ROOTDATALIST = "ROOTDATALIST";
    /**
     * LIST标签（关联表）
     */
    public static final String XML_COLUMN_TYPE_DATALIST = "DATALIST";
    /**
     * 单条记录标签
     */
    public static final String XML_COLUMN_TYPE_DATAITEM = "DATAITEM";
    /**
     * 数据库字段
     */
    public static final String XML_COLUMN_TYPE_DBCOLUMN = "DBCOLUMN";
    /**
     * 其他标签类型
     */
    public static final String XML_COLUMN_TYPE_OTHER = "OTHER";
    /***************************xml标签的数据类型Enum-end***************************/
    /***************************数据发送类型-start***************************/
    /**
     * 增量
     */
    public static final String SEND_TYPE_ALL = "ALL";
    /**
     * 全量
     */
    public static final String SEND_TYPE_INC = "INC";
    /**
     * 心跳
     */
    public static final String SEND_TYPE_HEARTBEAT = "HEARTBEAT";
    /***************************数据发送类型-end***************************/
    /***************************消息传递模式-start***************************/
    /**
     * 点对点模式
     */
    public static final String PROCESS_MODE_TOPIC = "TOPIC";
    /**
     * 发布订阅模式
     */
    public static final String PROCESS_MODE_QUEUE = "QUEUE";
    /***************************消息传递模式-end***************************/
    /***************************日期字段类型-start***************************/
    /**
     * DATE型
     */
    public static final String DATE_TYPE_DATE = "DATE";
    /**
     * CLOB型
     */
    public static final String DATE_TYPE_CLOB = "CLOB";
    /**
     * VARCHAR型
     */
    public static final String DATE_TYPE_VARCHAR = "VARCHAR";
    /***************************日期字段类型-end***************************/
    /**
     * 限制交通流字段
     */
    public static final String TRN_TRFD = "TRFD";
    /**
     * 豁免交通流字段
     */
    public static final String TRN_EXPD = "EXPD";

    //输入类型
    public static final String ACTIVEMQ = "activemq";
    public static final String IBMMQ = "ibmmq";
    public static final String TCPIP = "tcpip";
    public static final String DATABASE = "database";

    //表配置
    public static final String ACTIVEMQCONFIG = "ACTIVEMQCONFIG";
    public static final String IBMMQCONFIG = "IBMMQCONFIG";
    public static final String FTPCONFIG = "FTPCONFIG";
    public static final String TCPIPCONFIG = "TCPIPCONFIG";

    /**
     * TCP连接状态信号量的阈值
     */
    public static final Integer SEMAPHORE_NUM = 1;

    /**
     * freemarker模板
     */
    public static Map<Long, Date> ftlTemplateUpdateTimeMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
    public static Map<Long, Template> ftlTemplateMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);

    /**
     * acativeMq连接信息
     */
    public static Map<Long, Date> activemqUpdateTimeMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
    public static Map<Long, ActiveMQMessageProducer> messageProducerMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
    public static Map<Long, JmsTemplate> jmsTemplateMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
    public static Map<Long, ActiveMQConnection> activeMQConnectionMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
    /**
     * ibm连接
     */
    public static Map<Long, Date> ibmmqUpdateTimeMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
    public static Map<Long, CachingConnectionFactory> ibmMQConnectionMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
    /**
     * TCPIP通道
     */
    public static Map<Long, Date> tcpipUpdateTimeMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
    public static Map<Long, Channel> tcpipChannelMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
    /**
     * TCP连接ip索引
     */
    public static Map<Long, Integer> tcpipIndexMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
    public static Map<Long, Semaphore> tcpipSemaphoreMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
    /**
     * ftp连接信息
     */
    public static Map<Long, Date> ftpUpdateTimeMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
    public static Map<Long, FtpMsgSender> ftpMsgSenderMap = new HashedMap();
    public static Map<Long, FtpUtil> ftpUtilMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
}