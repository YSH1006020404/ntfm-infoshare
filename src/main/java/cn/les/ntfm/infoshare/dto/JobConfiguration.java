package cn.les.ntfm.infoshare.dto;

import cn.les.ntfm.infoshare.entity.DataBaseRelationDO;
import cn.les.ntfm.infoshare.entity.JobRunDetailDO;
import cn.les.ntfm.infoshare.entity.XmlFormatConfigDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * job配置详情
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-03-13 16:22
 */
@Getter
@Setter
@ToString
public class JobConfiguration implements Serializable {
    private static final long serialVersionUID = 8461222394788771237L;
    /**
     * 链路唯一标识
     */
    private Long linkID;
    /**
     * 交互标识
     */
    private String interactionMark;
    /**
     * job分组
     */
    private String jobGroup;
    /**
     * job名称
     */
    private String jobName;
    /**
     * job执行频率
     */
    private String frequency;
    /**
     * 报文格式配置
     */
    private List<XmlFormatConfigDO> xmlFormatConfigList;
    /**
     * LIST标签（基础表）
     */
    private String rootDataList;
    /**
     * 每条报文封装的数据量
     */
    private int splitNum;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 是否有关联表
     */
    private Boolean hasRelationTables;
    /**
     * 关联表检索脚本(key:关联表配置ID value:关联表检索脚本)
     */
    private Map<Long, String> relationTableSelectSqlAllMap;
    /**
     * 关联表的XML集合标签(key:关联表配置ID value:关联表XML集合标签)
     */
    private Map<Long, String> relationTableDataLabelMap;
    /**
     * 关联表信息(key:关联表配置ID value:关联表配置详情)
     */
    private Map<Long, DataBaseRelationDO> relationTableMap;
    /**
     * 基础表全量检索脚本
     */
    private String baseTableSelectSqlAll;
    /**
     * 更新时间的实体
     */
    private JobRunDetailDO jobRunDetail;
    /**
     * 去重表数据更新脚本
     */
    private String mergeSql;
    /**
     * 基础表增量检索脚本
     */
    private String baseTableSelectSqlInc;
    /**
     * 增量发送是否去重
     */
    private Boolean deduplicationFlag;
    /**
     * job发送数据目的地配置
     */
    private Map<Long, HashMap<String, Object>> destinationConfigMaps;
    /**
     * 数据输出的发送类
     */
    private Map<Long, String> sendTypeServiceMap;
    /**
     * freemarker模板相关的最新更新时间
     */
    private Date ftlTemplateUpdateTime;
    /**
     * 数据输出的最新更新时间
     */
    private Map<Long, Date> destinationUpdateTime;
    /**
     * 基础表中的CLOB字段
     */
    private List<String> basetableClobColumns;
    /**
     * 关联表中的CLOB字段
     */
    private Map<Long, List<String>> relationTableClobColumns;
}
