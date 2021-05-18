package cn.les.ntfm.infoshare.dao;

import cn.les.ntfm.constant.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用Mapper
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-03-14 16:15
 */
@Mapper
public interface CommonMapper {
    /**
     * 根据主键和表名检索配置信息
     *
     * @param tableName 表名
     * @param id        主键
     * @return 配置信息
     */
    HashMap<String, Object> getConfigurationById(@Param("tableName") String tableName,
                                                 @Param("id") Long id);

    /**
     * 检索输出数据
     *
     * @param listSql       检索脚本
     * @param time          上次更新时间
     * @param relationValue 关联字段的值
     * @return 输出数据
     */
    List<Map<String, Object>> listOutputData(@Param("listSql") String listSql,
                                             @Param("time") Date time,
                                             @Param("linkid") Long linkid,
                                             @Param(Constants.SQL_RELATION_COLUMN_PARAM) String relationValue);

    /**
     * 更新C_去重表
     *
     * @param mergeSql 更新脚本
     * @param time     上次更新时间
     * @param linkId   链路ID
     */
    void mergeData(@Param("mergeSql") String mergeSql,
                   @Param("time") Date time,
                   @Param(Constants.SQL_LINKID) Long linkId);

    /**
     * 数据检索
     *
     * @param querySql 检索内容
     * @return 检索结果
     */
    Map queryData(@Param("querySql") String querySql);

    /**
     * 调用存储过程
     *
     * @param proName 存储过程名称
     */
    void callProByName(@Param("proName") String proName);

    /**
     * 主键删除
     *
     * @param tableName 表名
     * @param id        主键
     */
    void deleteDataById(@Param("tableName") String tableName,
                        @Param("id") Long id);

    /**
     * 检索指定表的所有字段
     *
     * @param tableName 表名
     * @return
     */
    List<Map<String, String>> listColumns(@Param("tableName") String tableName);
}