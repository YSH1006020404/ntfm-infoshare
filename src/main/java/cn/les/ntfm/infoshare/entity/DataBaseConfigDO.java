package cn.les.ntfm.infoshare.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 数据抽取配置
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-20 14:27
 */
@Getter
@Setter
@ToString
public class DataBaseConfigDO extends BaseEntity {
    /**
     * 全量发送频率
     */
    private String allFrequency;
    /**
     * 增量发送频率
     */
    private String incFrequency;
    /**
     * XML模板的关联主键
     */
    private Long xmltemplateId;
    /**
     * XML配置的关联主键
     */
    private Long xmlformatconfigId;
    /**
     * XML最大组装数据量
     */
    private Integer splitNum;
    /**
     * 是否发送心跳(0:否 1:是)
     */
    private Boolean beatFlag;
    /**
     * 增量发送是否去重(0:否 1:是)
     */
    private Boolean deduplicationFlag;
    /**
     * 数据抽取表的主键关联
     */
    private Long tableId;
    /**
     * 数据抽取表的表名
     */
    private String tableName;
    /**
     * 筛选条件
     */
    private String condition;
    /**
     * 关联表
     */
    private List<DataBaseRelationDO> dataBaseRelationList;
}