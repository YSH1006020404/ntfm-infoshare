package cn.les.ntfm.infoshare.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 数据输出关联表
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-03-13 11:55
 */
@Getter
@Setter
@ToString
public class OutputRelationDO extends BaseEntity {
    /**
     * 输出字段名称
     */
    private String columnName;
    /**
     * 输出字段的数据类型
     */
    private String columnType;
    /**
     * 日期输出格式
     */
    private String dateFormat;
    /**
     * 注释
     */
    private String explain;
    /**
     * 是否为空(0:否 1:是)
     */
    private Boolean nullable;
}