package cn.les.ntfm.infoshareConf.service.impl;

import cn.les.framework.web.base.impl.BaseServiceImpl;
import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.constant.PropertyNameConstants;
import cn.les.ntfm.infoshare.entity.*;
import cn.les.ntfm.infoshare.dao.*;
import cn.les.ntfm.infoshareConf.service.DataSourceConfigService;
import cn.les.ntfm.util.Log4jUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据源配置service
 *
 * @author 杨硕
 * @date 2020-06-08-上午11:07
 */
@Service("dataSourceConfigService")
public class DataSourceConfigServiceImpl extends BaseServiceImpl implements DataSourceConfigService {
    @Resource
    private DatabaseConfigMapper databaseConfigMapper;
    @Resource
    private DatabaseRelationMapper databaseRelationMapper;
    @Resource
    private XmlTemplateMapper xmlTemplateMapper;
    @Resource
    private XmlFormatConfigMapper xmlFormatConfigMapper;
    @Resource
    private OutputRelationMapper outputRelationMapper;
    @Resource
    private InfoShareConfigMapper infoShareConfigMapper;
    @Resource
    private CommonMapper commonMapper;
    @Resource
    private PropertyDictMapper propertyDictMapper;

    @Override
    public Map<String, Object> getDataByInfoshareId(Long infoshareId) {
        Map<String, Object> result = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
        InfoshareConfigDO infoshareConfigDO = infoShareConfigMapper.getDataById(infoshareId);
        result.put("infoshareConfig", infoshareConfigDO);
        DataBaseConfigDO dataBaseConfig = databaseConfigMapper.getDataByInfoshareId(infoshareId);
        if (dataBaseConfig != null) {
            result.put("dataBaseConfig", dataBaseConfig);
            List<DataBaseRelationDO> dataBaseRelationList = databaseRelationMapper.listByDatabaseconfigId(dataBaseConfig.getId());
            result.put("dataBaseRelationList", dataBaseRelationList);
        }
        return result;
    }

    @Override
    public void saveDataBaseConfig(JSONObject paramMap) throws Exception {
        try {
            DataBaseConfigDO dataBaseConfigDO = JSON.parseObject(JSON.toJSONString(paramMap.get("dataBaseConfig")), DataBaseConfigDO.class);
            //根据XML模板初始化XML配置
            Long pid = recursiveXmlFormat(null, xmlTemplateMapper.getDataById(dataBaseConfigDO.getXmltemplateId()));
            //新增数据源配置表
            dataBaseConfigDO.setXmlformatconfigId(pid);
            databaseConfigMapper.addData(dataBaseConfigDO);

            //新增关联表
            ArrayList<Map> dataBaseRelation = (ArrayList<Map>) paramMap.get("dataBaseRelation");
            if (dataBaseRelation != null && dataBaseRelation.size() > 0) {
                for (Map o : dataBaseRelation) {
                    DataBaseRelationDO dataBaseRelationDO = JSON.parseObject(JSON.toJSONString(o), DataBaseRelationDO.class);
                    dataBaseRelationDO.setDatabaseconfigId(dataBaseConfigDO.getId());
                    databaseRelationMapper.addData(dataBaseRelationDO);
                }
            }
            //更新INFOSHARECONFIG
            InfoshareConfigDO infoshareConfigDO = JSON.parseObject(JSON.toJSONString(paramMap.get("infoshareConfig")), InfoshareConfigDO.class);
            Long sourceType = infoshareConfigDO.getSourceType();
            infoshareConfigDO = infoShareConfigMapper.getDataById(infoshareConfigDO.getId());
            infoshareConfigDO.setSourceType(sourceType);
            infoshareConfigDO.setSourceId(dataBaseConfigDO.getId());
            infoShareConfigMapper.updateDataById(infoshareConfigDO);
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("DataSourceConfigServiceImpl类的saveDataBaseConfig方法出现错误，错误原因：", e);
            throw new Exception("新增数据源配置失败！详情请查看日志！");
        }

    }

    @Override
    public void updateDataBaseConfig(JSONObject paramMap) throws Exception {
        try {
            DataBaseConfigDO dataBaseConfigDO = JSON.parseObject(JSON.toJSONString(paramMap.get("dataBaseConfig")), DataBaseConfigDO.class);
            DataBaseConfigDO dataBaseConfigOld = databaseConfigMapper.getDataById(dataBaseConfigDO.getId());
            //XML模板重置
            if (!dataBaseConfigOld.getXmltemplateId().equals(dataBaseConfigDO.getXmltemplateId())) {
                //删除原XML配置和OUTPUTRELATION
                if (dataBaseConfigOld.getXmlformatconfigId() != null) {
                    deleteDataBaseTable(dataBaseConfigOld.getXmlformatconfigId());
                }
                //将模板的配置MERGE到XML配置
                Long pid = recursiveXmlFormat(null, xmlTemplateMapper.getDataById(dataBaseConfigDO.getXmltemplateId()));
                dataBaseConfigDO.setXmlformatconfigId(pid);
            }
            //删除基础表相关配置
            if (!dataBaseConfigOld.getTableId().equals(dataBaseConfigDO.getTableId())) {
                deleteDataBaseTable(dataBaseConfigOld.getXmlformatconfigId());
            }

            //更新关联表相关配置
            List<DataBaseRelationDO> dataBaseRelationsOld = databaseRelationMapper.listByDatabaseconfigId(dataBaseConfigDO.getId());
            ArrayList<Map> dataBaseRelations = (ArrayList<Map>) paramMap.get("dataBaseRelation");
            if (dataBaseRelationsOld != null && dataBaseRelationsOld.size() > 0) {
                List<DataBaseRelationDO> deleteDatas = new ArrayList<>();
                //更新关联表
                for (DataBaseRelationDO dataBaseRelationOld : dataBaseRelationsOld) {
                    //是否删除标识
                    Boolean deleteFlag = true;
                    if (dataBaseRelations != null && dataBaseRelations.size() > 0) {
                        for (Map o : dataBaseRelations) {
                            DataBaseRelationDO dataBaseRelationDO = JSON.parseObject(JSON.toJSONString(o), DataBaseRelationDO.class);
                            if (dataBaseRelationOld.getId().equals(dataBaseRelationDO.getId())) {
                                deleteFlag = false;
                                databaseRelationMapper.updataDataById(dataBaseRelationDO);
                            }
                        }
                    }
                    if (deleteFlag) {
                        deleteDatas.add(dataBaseRelationOld);
                    }
                }
                //删除关联表
                List<XmlFormatConfigDO> xmlColumnTypeDatalist = xmlFormatConfigMapper.listAllXmlFormatConfigById(
                        dataBaseConfigOld.getXmlformatconfigId(), Constants.XML_COLUMN_TYPE_DATALIST);
                for (DataBaseRelationDO deleteData : deleteDatas) {
                    List<OutputRelationDO> outputRelationList = outputRelationMapper.listRelationTableData(deleteData.getTableId(), dataBaseConfigOld.getXmlformatconfigId());
                    if (outputRelationList != null && outputRelationList.size() > 0) {
                        for (OutputRelationDO outputRelationDO : outputRelationList) {
                            //删除输出字段配置
                            outputRelationMapper.deleteDataById(outputRelationDO.getId());
                        }
                    }
                    //删除XML配置
                    if (xmlColumnTypeDatalist != null && xmlColumnTypeDatalist.size() > 0) {
                        for (XmlFormatConfigDO xmlFormatConfigDO : xmlColumnTypeDatalist) {
                            if (deleteData.getTableId().equals(xmlFormatConfigDO.getTableId())) {
                                XmlFormatConfigDO xmlFormatConfigDelete = new XmlFormatConfigDO();
                                xmlFormatConfigDelete.setId(xmlFormatConfigDO.getId());
                                removeChildrenXmlFormat(xmlFormatConfigDelete);
                                break;
                            }
                        }
                    }
                    //删除关联表
                    databaseRelationMapper.deleteDataById(deleteData.getId());
                }
            }
            //新增关联表
            if (dataBaseRelations != null && dataBaseRelations.size() > 0) {
                for (Map o : dataBaseRelations) {
                    DataBaseRelationDO dataBaseRelationDO = JSON.parseObject(JSON.toJSONString(o), DataBaseRelationDO.class);
                    if (dataBaseRelationDO.getId() == null) {
                        dataBaseRelationDO.setDatabaseconfigId(dataBaseConfigDO.getId());
                        databaseRelationMapper.addData(dataBaseRelationDO);
                    }
                }
            }
            //更新数据源配置表
            databaseConfigMapper.updataDataById(dataBaseConfigDO);
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("DataSourceConfigServiceImpl类的updateDataBaseConfig方法出现错误，错误原因：", e);
            throw new Exception("修改数据源配置失败！详情请查看日志！");
        }
    }

    /**
     * 删除基础表关联的XML配置和字段输出配置
     *
     * @param xmlformatconfigId XML根节点
     */
    private void deleteDataBaseTable(Long xmlformatconfigId) {
        List<XmlFormatConfigDO> rootDataList = xmlFormatConfigMapper.listAllXmlFormatConfigById(xmlformatconfigId, Constants.XML_COLUMN_TYPE_ROOTDATALIST);
        if (rootDataList != null && rootDataList.size() > 0) {
            removeChildrenXmlFormat(rootDataList.get(0));
        }
    }

    /**
     * 递归新增XML配置表
     *
     * @param pid         父键
     * @param xmlTemplate XML模板
     */
    private Long recursiveXmlFormat(Long pid, XmlTemplateDO xmlTemplate) {
        XmlFormatConfigDO xmlFormatConfigDO = new XmlFormatConfigDO();
        xmlFormatConfigDO.setLabelName(xmlTemplate.getLabelName());
        xmlFormatConfigDO.setLabelType(xmlTemplate.getLabelType());
        xmlFormatConfigDO.setDisplaySeq(xmlTemplate.getDisplaySeq());
        xmlFormatConfigDO.setValue(xmlTemplate.getValue());
        if (xmlTemplate.getPid() != null) {
            xmlFormatConfigDO.setPid(pid);
        }
        xmlFormatConfigMapper.addData(xmlFormatConfigDO);
        List<XmlTemplateDO> xmlTemplateList = xmlTemplateMapper.listByPid(xmlTemplate.getId());
        if (xmlTemplateList != null && xmlTemplateList.size() > 0) {
            for (XmlTemplateDO xmlTemplateDO : xmlTemplateList) {
                recursiveXmlFormat(xmlFormatConfigDO.getId(), xmlTemplateDO);
            }
        }
        return xmlFormatConfigDO.getId();
    }

    @Override
    public void removeChildrenXmlFormat(XmlFormatConfigDO xmlFormatConfigDelete) {
        List<XmlFormatConfigDO> xmlFormatConfigList = xmlFormatConfigMapper.listXmlFormatConfig(xmlFormatConfigDelete);
        if (xmlFormatConfigList != null && xmlFormatConfigList.size() > 0) {
            for (XmlFormatConfigDO xmlFormatConfigDO : xmlFormatConfigList) {
                //删除输出字段配置
                if (xmlFormatConfigDO.getOutputrelationId() != null) {
                    outputRelationMapper.deleteDataById(xmlFormatConfigDO.getOutputrelationId());
                }
                //删除XML配置
                xmlFormatConfigMapper.deleteDataById(xmlFormatConfigDO.getId());
            }
        }
    }

    /**
     * 脚本正确性校验
     *
     * @param tableName 表名
     * @param condition 条件
     * @return Boolean
     */
    @Override
    public Boolean validateCondition(String tableName, String condition) {
        StringBuilder sqlBuilder = new StringBuilder(Constants.SQL_SELECT_CONSTANT);
        sqlBuilder.append(tableName);
        if (StringUtils.isNotEmpty(condition)) {
            sqlBuilder.append(Constants.SQL_WHERE);
            sqlBuilder.append(condition);
        }
        try {
            commonMapper.queryData(sqlBuilder.toString());
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("DataSourceConfigServiceImpl类的checkSql方法出现错误，错误原因：", e);
            return false;
        }
        return true;
    }

    /**
     * 校验关联条件
     *
     * @param dataSource 数据源配置
     */
    @Override
    public Boolean validateRelations(JSONObject dataSource) {
        StringBuilder sqlBuilder = new StringBuilder(Constants.SQL_SELECT_CONSTANT);
        DataBaseConfigDO dataBaseConfigDO = JSON.parseObject(JSON.toJSONString(dataSource.get("dataBaseConfig")), DataBaseConfigDO.class);
        sqlBuilder.append(dataBaseConfigDO.getTableName());
        DataBaseRelationDO dataBaseRelationDO = JSON.parseObject(JSON.toJSONString(dataSource.get("dataBaseRelation")), DataBaseRelationDO.class);
        sqlBuilder.append(Constants.SQL_LEFT_JOIN);
        sqlBuilder.append(dataBaseRelationDO.getTableName());
        sqlBuilder.append(Constants.SQL_ON);
        sqlBuilder.append(dataBaseConfigDO.getTableName()).append(Constants.DOT).append(dataBaseRelationDO.getBaseTableColumn());
        sqlBuilder.append(Constants.SQL_EQUALS);
        sqlBuilder.append(dataBaseRelationDO.getTableName()).append(Constants.DOT).append(dataBaseRelationDO.getRelationTableColumn());
        try {
            commonMapper.queryData(sqlBuilder.toString());
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("DataSourceConfigServiceImpl类的validateRelationTableCondition方法出现错误，错误原因：", e);
            return false;
        }
        return true;
    }

    /**
     * 获取默认数据接收方式（数据库）
     *
     * @return PropertyDictDO
     */
    @Override
    public PropertyDictDO getDefaultSourceType() {
        return propertyDictMapper.getPropertyDictByTypeNameAndDictName(
                PropertyNameConstants.SOURCE_TYPE
                , PropertyNameConstants.DATABASE);
    }

}
