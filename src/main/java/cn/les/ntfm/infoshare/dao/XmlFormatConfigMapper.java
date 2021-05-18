package cn.les.ntfm.infoshare.dao;

import cn.les.ntfm.infoshare.entity.InfoshareConfigDO;
import cn.les.ntfm.infoshare.entity.XmlFormatConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * xml格式配置Mapper
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-25 15:29
 */
@Mapper
public interface XmlFormatConfigMapper {
    /**
     * 新增
     *
     * @param xmlFormatConfigDO XML配置
     */
    void addData(XmlFormatConfigDO xmlFormatConfigDO);

    /**
     * 主键修改
     *
     * @param xmlFormatConfigDO XML配置
     */
    void updateDataById(XmlFormatConfigDO xmlFormatConfigDO);

    /**
     * 主键检索
     *
     * @param id 主键
     * @return XmlFormatConfigDO
     */
    XmlFormatConfigDO getDataById(@Param(value = "id") Long id);

    /**
     * 主键删除
     *
     * @param id XML主键
     */
    void deleteDataById(@Param(value = "id") Long id);

    /**
     * 根据输出字段配置ID删除关联XML配置
     *
     * @param outputrelationId 输出字段配置ID
     */
    void deleteDataByOutputrelationId(@Param(value = "outputrelationId") Long outputrelationId);

    /**
     * 获取所有xml配置信息
     *
     * @param xmlFormatConfigDO XML配置
     * @return List<XmlFormatConfigDO>
     */
    List<XmlFormatConfigDO> listXmlFormatConfig(XmlFormatConfigDO xmlFormatConfigDO);

    /**
     * 获取下一层子节点
     *
     * @param pid       父键
     * @param labelType 字节点的节点类型
     * @return List<XmlFormatConfigDO>
     */
    List<XmlFormatConfigDO> listByPid(@Param(value = "pid") Long pid, @Param(value = "labelType") String labelType);


    /**
     * 根据主键获取DATAITEM父节点
     *
     * @param id 主键
     * @return XmlFormatConfigDO
     */
    XmlFormatConfigDO getDataItemById(@Param(value = "id") Long id);

    /**
     * 根据XML配置主键获取链路信息
     *
     * @param xmlformatId XML配置主键
     * @return InfoshareConfigDO
     */
    InfoshareConfigDO getInfoshareByXmlformatId(@Param(value = "xmlformatId") Long xmlformatId);

    /**
     * 获取父节点数量
     *
     * @param id        主键
     * @param labelType 标签类型
     * @return list
     */
    List<XmlFormatConfigDO> listParentXmlFormatConfig(@Param(value = "id") Long id, @Param(value = "labelType") String labelType);

    /**
     * 根据主键获取所有关联XML的配置
     *
     * @param id        主键
     * @param labelType 标签类型
     * @return list
     */
    List<XmlFormatConfigDO> listAllXmlFormatConfigById(@Param(value = "id") Long id, @Param(value = "labelType") String labelType);

    /**
     * 获取影响freemarker模板的配置的最新更新时间
     *
     * @param xmlformatId XML配置主键
     * @return Date
     */
    Date getFreemarkerUpdateTime(@Param(value = "xmlformatId") Long xmlformatId);
}
