package cn.les.ntfm.infoshare.dao;

import cn.les.ntfm.infoshare.entity.InfoshareConfigDO;
import cn.les.ntfm.infoshare.entity.XmlTemplateDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * xml模板配置Mapper
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-25 15:29
 */
@Mapper
public interface XmlTemplateMapper {
    /**
     * 新增
     *
     * @param xmlTemplateDO
     */
    void addData(XmlTemplateDO xmlTemplateDO);

    /**
     * 主键修改
     *
     * @param xmlTemplateDO
     */
    void updateDataById(XmlTemplateDO xmlTemplateDO);

    /**
     * 主键检索
     *
     * @param id
     * @return
     */
    XmlTemplateDO getDataById(@Param(value = "id") Long id);

    /**
     * 删除本级及下级节点
     *
     * @param id
     */
    void deleteDataByPid(@Param(value = "id") Long id);

    /**
     * 获取所有xml配置信息
     *
     * @return
     */
    List<XmlTemplateDO> listXmlTemplate(@Param(value = "id") Long id);


    /**
     * 获取XML配置文件列表
     *
     * @param id
     * @return
     */
    List<Map<Long, String>> listJsonData(@Param(value = "id") Long id);

    /**
     * 获取下一层子节点
     *
     * @param pid
     * @return
     */
    List<XmlTemplateDO> listByPid(@Param(value = "pid") Long pid);

    /**
     * 获取已关联该模板的链路
     *
     * @param id
     * @return
     */
    List<InfoshareConfigDO> listAffectedLinks(@Param(value = "id") Long id);
}
