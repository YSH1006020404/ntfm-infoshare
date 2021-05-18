package cn.les.ntfm.infoshare.dao;

import cn.les.ntfm.infoshare.entity.DataBaseConfigDO;
import cn.les.ntfm.infoshare.entity.DataBaseRelationDO;
import cn.les.ntfm.infoshare.entity.OutputRelationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据输出字段关联表Mapper
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-03-13 15:44
 */
@Mapper
public interface OutputRelationMapper {
    /**
     * 新增
     *
     * @param outputRelationDO
     */
    void addData(OutputRelationDO outputRelationDO);

    /**
     * 主键修改
     *
     * @param outputRelationDO
     */
    void updateById(OutputRelationDO outputRelationDO);

    /**
     * 主键检索
     *
     * @param id
     * @return
     */
    OutputRelationDO getDataById(Long id);

    /**
     * 主键删除
     *
     * @param id
     */
    void deleteDataById(Long id);

    /**
     * 获取相关表的输出字段
     *
     * @param tableId           表字典项ID
     * @param xmlFormatConfigId XML配置根节点ID
     * @return list
     */
    List<OutputRelationDO> listRelationTableData(@Param("tableId") Long tableId, @Param("xmlFormatConfigId") Long xmlFormatConfigId);

}
