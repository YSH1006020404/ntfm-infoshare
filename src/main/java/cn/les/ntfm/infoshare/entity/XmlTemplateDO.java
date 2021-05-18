package cn.les.ntfm.infoshare.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * xml模板配置
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-25 15:32
 */
@Setter
@Getter
@ToString
public class XmlTemplateDO extends BaseEntity {
    /**
     * 父键
     */
    private Long pid;
    /**
     * 显示顺序
     */
    private Integer displaySeq;
    /**
     * 节点名称
     */
    private String labelName;
    /**
     * 节点类型（TXT:文本; SUBTYPE:消息子类型; YYYYMMDDHHMMSS:时间年月日时分秒;SERIAL:流水号;
     * STATISTICS:报文数据量统计：全量/增量(ALL/INC)_一次任务报文数据量_报文当前页数/报文总页数_当前报文数据量;
     * ROOTDATALIST:LIST标签（基础表）; DATALIST:LIST标签（关联表）; DATAITEM:单条记录标签; DBCOLUMN:数据库字段; OTHER:其他标签类型）
     */
    private String labelType;
    /**
     * 值
     */
    private String value;
    /**
     * xml名称
     */
    private String displayName;
}