package cn.les.ntfm.infoshare.dao;

import cn.les.ntfm.infoshare.entity.DataBaseRelationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据源详情关联表Mapper
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-22 10:44
 */
@Mapper
public interface DatabaseRelationMapper {
    /**
     * 新增
     *
     * @param dataBaseRelationDO
     */
    void addData(DataBaseRelationDO dataBaseRelationDO);

    /**
     * 主键修改
     *
     * @param dataBaseRelationDO
     */
    void updataDataById(DataBaseRelationDO dataBaseRelationDO);

    /**
     * 主键删除
     *
     * @param id
     */
    void deleteDataById(Long id);

    /**
     * 根据数据源配置ID删除关联表
     *
     * @param databaseconfigId
     */
    void deleteDataByDatabaseconfigId(@Param("databaseconfigId") Long databaseconfigId);

    /**
     * 主键检索
     *
     * @param id
     * @return
     */
    DataBaseRelationDO getDataById(@Param("id") Long id);

    /**
     * 根据数据源配置检索关联表
     *
     * @param databaseconfigId
     * @return
     */
    List<DataBaseRelationDO> listByDatabaseconfigId(@Param("databaseconfigId") Long databaseconfigId);


}
