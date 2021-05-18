package cn.les.ntfm.infoshare.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 字典项详情
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-03-13 11:47
 */
@Getter
@Setter
@ToString
public class PropertyDictDO extends BaseEntity {
    /**
     * 领域属性id
     */
    private Long propertyTypeId;
    /**
     * 显示顺序
     */
    private Integer displaySeq;
    /**
     * 显示名称
     */
    private String displayName;
    /**
     * 值
     */
    private String dictValue;
    /**
     * 关联表
     */
    private String relationTable;
    /**
     * 唯一标识
     */
    private String columnLabel;
}