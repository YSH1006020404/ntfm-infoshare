package cn.les.ntfm.infoshareConf.service;

import cn.les.ntfm.infoshare.entity.InfoshareConfigDO;
import cn.les.ntfm.infoshare.entity.XmlFormatConfigDO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * XML配置service
 *
 * @author 杨硕
 * @date 2020-06-03-下午2:54
 */
public interface XmlFormatService {

    /**
     * 新增XML配置信息
     *
     * @param param 数据库配置ID和XML配置
     */
    XmlFormatConfigDO saveXmlFormat(JSONObject param) throws Exception;

    /**
     * 修改XML配置信息
     *
     * @param param 数据库配置ID和XML配置
     */
    XmlFormatConfigDO updateXmlFormat(JSONObject param) throws Exception;

    /**
     * 删除XML配置
     *
     * @param ids 主键
     */
    void removeXmlFormat(Long[] ids) throws Exception;

    /**
     * 主键检索XML配置
     *
     * @param id 主键
     * @return map
     */
    Map<String, Object> getXmlFormatById(Long id);

    /**
     * 检索XML配置
     *
     * @param xmlFormatConfigId XML配置父键
     * @return list
     */
    List<XmlFormatConfigDO> listXmlFormatConfig(Long xmlFormatConfigId);

    /**
     * 根据XML配置获取链路配置
     *
     * @param xmlFormatConfigId XML主键
     * @return InfoshareConfigDO
     */
    InfoshareConfigDO getInfoshareByXmlformatId(Long xmlFormatConfigId);

    /**
     * 根据父节点获取可选择的XML标签类型
     *
     * @param pid 父节点ID
     * @return JSONArray
     */
    JSONArray getLableTypeByPid(Long pid);

    /**
     * 判断是否可以增加子节点
     *
     * @param pid 父节点ID
     * @return boolean
     */
    Boolean getAddXmlFormatConfigAbility(Long pid);

    /**
     * 根据父键获取关联表信息
     *
     * @param pid 父键
     * @return map
     */
    Map<String, Object> getTableInfoByPid(Long pid);

    /**
     * 获取可用的数据源表
     * @param databaseConfigId
     * @param labelType
     * @return
     */
    List<Map<String, Object>> listAvailableTables(Long databaseConfigId, String labelType);
}
