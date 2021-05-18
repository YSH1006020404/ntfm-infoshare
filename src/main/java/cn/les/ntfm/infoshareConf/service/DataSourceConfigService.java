package cn.les.ntfm.infoshareConf.service;


import cn.les.ntfm.infoshare.entity.PropertyDictDO;
import cn.les.ntfm.infoshare.entity.XmlFormatConfigDO;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * 数据源配置service
 *
 * @author 杨硕
 * @date 2020-06-08-上午11:02
 */
public interface DataSourceConfigService {
    /**
     * 根据infoshareId获取数据源配置
     *
     * @param infoshareId 链路ID
     * @return
     */
    Map<String, Object> getDataByInfoshareId(Long infoshareId);

    /**
     * 新增
     *
     * @param data 数据源详情
     */
    void saveDataBaseConfig(JSONObject data) throws Exception;

    /**
     * 主键修改
     *
     * @param data 数据源详情
     */
    void updateDataBaseConfig(JSONObject data) throws Exception;

    /**
     * 删除相关的输出字段和XML配置
     *
     * @param xmlFormatConfigDelete 删除参数
     */
    void removeChildrenXmlFormat(XmlFormatConfigDO xmlFormatConfigDelete);

    /**
     * 脚本正确性校验
     *
     * @param tableName 表名
     * @param condition 条件
     * @return Boolean
     */
    Boolean validateCondition(String tableName, String condition);

    /**
     * 校验关联条件
     *
     * @param dataSource 数据源配置
     */
    Boolean validateRelations(JSONObject dataSource);

    /**
     * 获取默认数据接收方式（数据库）
     *
     * @return PropertyDictDO
     */
    PropertyDictDO getDefaultSourceType();
}
