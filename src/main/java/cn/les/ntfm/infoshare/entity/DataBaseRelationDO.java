package cn.les.ntfm.infoshare.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 数据源详情关联表
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-20 14:27
 */
@Getter
@Setter
@ToString
public class DataBaseRelationDO extends BaseEntity {
    /**
     * 数据源配置表的主键
     */
    private Long databaseconfigId;
    /**
     * 关联表的主键
     */
    private Long tableId;
    /**
     * 关联表的表名
     */
    private String tableName;
    /**
     * 基础表的关联字段
     */
    private String baseTableColumn;
    /**
     * 关联表的关联字段
     */
    private String relationTableColumn;
    /**
     * 筛选条件
     */
    private String condition;
}