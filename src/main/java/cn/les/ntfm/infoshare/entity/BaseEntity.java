package cn.les.ntfm.infoshare.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 实体类基础信息
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-03-12 14:46
 */
@Getter
@Setter
@ToString
public class BaseEntity implements Serializable {
    /**
     * 主键
     */
    private Long id;
    /**
     * 创建用户
     */
    private String createUser;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新用户
     */
    private String updateUser;
    /**
     * 更新时间
     */
    private Date updateTime;
}