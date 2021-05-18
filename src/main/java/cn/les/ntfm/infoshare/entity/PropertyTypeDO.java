package cn.les.ntfm.infoshare.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 字典项类型
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-03-13 11:46
 */
@Getter
@Setter
@ToString
public class PropertyTypeDO {
    /**
     * ID
     */
    private Long id;
    /**
     * 领域属性
     */
    private String propertyName;
}