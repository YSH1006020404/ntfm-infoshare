package cn.les.ntfm.infoshare.dao;

import cn.les.ntfm.infoshare.entity.DataBaseConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 数据抽取配置Mapper
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-22 10:44
 */
@Mapper
public interface DatabaseConfigMapper {
    /**
     * 新增
     *
     * @param dataBaseConfigDO 配置信息
     */
    void addData(DataBaseConfigDO dataBaseConfigDO);

    /**
     * 主键更新
     *
     * @param dataBaseConfigDO 配置信息
     */
    void updataDataById(DataBaseConfigDO dataBaseConfigDO);

    /**
     * 主键删除
     *
     * @param id 主键
     */
    void deleteDataById(Long id);

    /**
     * 主键检索
     *
     * @param id 主键
     * @return DataBaseConfigDO
     */
    DataBaseConfigDO getDataById(@Param("id") Long id);

    /**
     * 根据链路ID检索数据源
     *
     * @param infoshareId 链路ID
     * @return DataBaseConfigDO
     */
    DataBaseConfigDO getDataByInfoshareId(@Param("infoshareId") Long infoshareId);

    /**
     * 根据数据源配置ID获取所有数据源表
     *
     * @param databaseConfigId 数据源配置ID
     * @return list
     */
    List<Map<String, Object>> listTablesByDatabaseConfigId(@Param("databaseConfigId") Long databaseConfigId);
}
