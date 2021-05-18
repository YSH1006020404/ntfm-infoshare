package cn.les.ntfm.infoshareConf.service.impl;

import cn.les.framework.web.base.impl.BaseServiceImpl;
import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.entity.*;
import cn.les.ntfm.infoshare.dao.*;
import cn.les.ntfm.infoshareConf.service.DataSourceConfigService;
import cn.les.ntfm.infoshareConf.service.XmlFormatService;
import cn.les.ntfm.util.Log4jUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 输出项配置service
 *
 * @author 杨硕
 * @date 2020-06-03-下午3:03
 */
@Service("xmlFormatService")
public class XmlFormatServiceImpl extends BaseServiceImpl implements XmlFormatService {
    @Resource
    private XmlFormatConfigMapper xmlFormatConfigMapper;
    @Resource
    private OutputRelationMapper outputRelationMapper;
    @Resource
    private DatabaseConfigMapper databaseConfigMapper;
    @Resource
    private DatabaseRelationMapper databaseRelationMapper;
    @Resource
    private DataSourceConfigService dataSourceConfigService;

    @Override
    public XmlFormatConfigDO saveXmlFormat(JSONObject param) throws Exception {
        try {
            //新增OUTPUTRELATION
            OutputRelationDO outputRelationDO = JSON.parseObject(JSON.toJSONString(param.get("outputRelation")), OutputRelationDO.class);
            XmlFormatConfigDO xmlFormatConfigDO = JSON.parseObject(JSON.toJSONString(param.get("xmlFormat")), XmlFormatConfigDO.class);
            if (Constants.XML_COLUMN_TYPE_DBCOLUMN.equals(xmlFormatConfigDO.getLabelType())) {
                outputRelationMapper.addData(outputRelationDO);
                xmlFormatConfigDO.setOutputrelationId(outputRelationDO.getId());
            }
            //新增XML配置
            xmlFormatConfigMapper.addData(xmlFormatConfigDO);
            return xmlFormatConfigMapper.getDataById(xmlFormatConfigDO.getId());
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("XmlFormatServiceImpl类的saveXmlFormat方法出现错误，错误原因：", e);
            throw new Exception("新增XML配置信息失败！详情请查看日志！");
        }
    }

    @Override
    public XmlFormatConfigDO updateXmlFormat(JSONObject param) throws Exception {
        try {
            //更新OUTPUTRELATION
            OutputRelationDO outputRelationDO = JSON.parseObject(JSON.toJSONString(param.get("outputRelation")), OutputRelationDO.class);
            XmlFormatConfigDO xmlFormatConfigDO = JSON.parseObject(JSON.toJSONString(param.get("xmlFormat")), XmlFormatConfigDO.class);
            if (Constants.XML_COLUMN_TYPE_DBCOLUMN.equals(xmlFormatConfigDO.getLabelType())) {
                outputRelationMapper.updateById(outputRelationDO);
            }
            //更新XML配置
            xmlFormatConfigMapper.updateDataById(xmlFormatConfigDO);
            return xmlFormatConfigMapper.getDataById(xmlFormatConfigDO.getId());
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("XmlFormatServiceImpl类的updateXmlFormat方法出现错误，错误原因：", e);
            throw new Exception("修改XML配置信息失败！详情请查看日志！");
        }
    }

    @Override
    public void removeXmlFormat(Long[] ids) throws Exception {
        try {
            for (Long id : ids) {
                XmlFormatConfigDO xmlFormatConfigDelete = new XmlFormatConfigDO();
                xmlFormatConfigDelete.setId(id);
                dataSourceConfigService.removeChildrenXmlFormat(xmlFormatConfigDelete);
            }
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("XmlFormatServiceImpl类的uremoveXmlFormat方法出现错误，错误原因：", e);
            throw new Exception("删除XML配置信息失败！详情请查看日志！");
        }
    }


    @Override
    public Map<String, Object> getXmlFormatById(Long id) {
        Map<String, Object> result = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
        XmlFormatConfigDO xmlFormatConfigDO = xmlFormatConfigMapper.getDataById(id);
        result.put("xmlFormat", xmlFormatConfigDO);
        result.put("outputRelation", outputRelationMapper.getDataById(xmlFormatConfigDO.getOutputrelationId()));
        return result;
    }

    @Override
    public List<XmlFormatConfigDO> listXmlFormatConfig(Long xmlFormatConfigId) {
        XmlFormatConfigDO xmlFormatConfigDO = new XmlFormatConfigDO();
        xmlFormatConfigDO.setId(xmlFormatConfigId);
        List<XmlFormatConfigDO> result = xmlFormatConfigMapper.listXmlFormatConfig(xmlFormatConfigDO);
        if (result != null && result.size() > 0) {
            for (XmlFormatConfigDO formatConfigDO : result) {
                formatConfigDO.setOutputRelation(outputRelationMapper.getDataById(formatConfigDO.getOutputrelationId()));
            }
        }
        return result;
    }

    @Override
    public InfoshareConfigDO getInfoshareByXmlformatId(Long xmlFormatConfigId) {
        return xmlFormatConfigMapper.getInfoshareByXmlformatId(xmlFormatConfigId);
    }

    @Override
    public JSONArray getLableTypeByPid(Long pid) {
        JSONArray result = getBaseLabelType();
        XmlFormatConfigDO parentXmlFormatConfig = xmlFormatConfigMapper.getDataById(pid);
        switch (parentXmlFormatConfig.getLabelType()) {
            case Constants.XML_COLUMN_TYPE_ROOTDATALIST:
            case Constants.XML_COLUMN_TYPE_DATALIST:
                setLabelEnabled(result, Constants.XML_COLUMN_TYPE_DATAITEM);
                break;
            case Constants.XML_COLUMN_TYPE_DATAITEM:
                List<XmlFormatConfigDO> datalistXmlFormatConfigList = xmlFormatConfigMapper.listParentXmlFormatConfig(pid, Constants.XML_COLUMN_TYPE_DATALIST);
                setLabelEnabled(result, Constants.XML_COLUMN_TYPE_TXT);
                setLabelEnabled(result, Constants.XML_COLUMN_TYPE_DBCOLUMN);
                setLabelEnabled(result, Constants.XML_COLUMN_TYPE_OTHER);
                if (datalistXmlFormatConfigList == null || datalistXmlFormatConfigList.size() == 0) {
                    setLabelEnabled(result, Constants.XML_COLUMN_TYPE_DATALIST);
                }
                break;
            case Constants.XML_COLUMN_TYPE_OTHER:
                List<XmlFormatConfigDO> parentXmlFormatConfigList = xmlFormatConfigMapper
                        .listParentXmlFormatConfig(pid, Constants.XML_COLUMN_TYPE_ROOTDATALIST);
                if (parentXmlFormatConfigList == null || parentXmlFormatConfigList.size() == 0) {
                    setLabelEnabled(result, Constants.XML_COLUMN_TYPE_TXT);
                    setLabelEnabled(result, Constants.XML_COLUMN_TYPE_SUBTYPE);
                    setLabelEnabled(result, Constants.XML_COLUMN_TYPE_YYYYMMDDHHMMSS);
                    setLabelEnabled(result, Constants.XML_COLUMN_TYPE_SERIAL);
                    setLabelEnabled(result, Constants.XML_COLUMN_TYPE_STATISTICS);
                    setLabelEnabled(result, Constants.XML_COLUMN_TYPE_OTHER);
                    List<XmlFormatConfigDO> rootdatalistXmlFormatConfigList = xmlFormatConfigMapper
                            .listAllXmlFormatConfigById(pid, Constants.XML_COLUMN_TYPE_ROOTDATALIST);
                    if (rootdatalistXmlFormatConfigList == null || rootdatalistXmlFormatConfigList.size() == 0) {
                        setLabelEnabled(result, Constants.XML_COLUMN_TYPE_ROOTDATALIST);
                    }
                } else {
                    setLabelEnabled(result, Constants.XML_COLUMN_TYPE_DATALIST);
                    setLabelEnabled(result, Constants.XML_COLUMN_TYPE_TXT);
                    setLabelEnabled(result, Constants.XML_COLUMN_TYPE_DBCOLUMN);
                    setLabelEnabled(result, Constants.XML_COLUMN_TYPE_OTHER);
                }
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    public Boolean getAddXmlFormatConfigAbility(Long pid) {
        List<XmlFormatConfigDO> childremXmlFormatConfigList = xmlFormatConfigMapper.listByPid(pid, null);
        if (childremXmlFormatConfigList == null || childremXmlFormatConfigList.size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Map<String, Object> getTableInfoByPid(Long pid) {
        List<XmlFormatConfigDO> dataitemXmlFormatConfigList = xmlFormatConfigMapper.listParentXmlFormatConfig(pid, Constants.XML_COLUMN_TYPE_DATAITEM);
        if (dataitemXmlFormatConfigList == null || dataitemXmlFormatConfigList.size() == 0) {
            return null;
        } else {
            Map<String, Object> result = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
            result.put("tableId", dataitemXmlFormatConfigList.get(0).getTableId());
            result.put("tableName", dataitemXmlFormatConfigList.get(0).getTableName());
            return result;
        }
    }

    @Override
    public List<Map<String, Object>> listAvailableTables(Long databaseConfigId, String labelType) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (Constants.XML_COLUMN_TYPE_ROOTDATALIST.equals(labelType)) {
            DataBaseConfigDO dataBaseConfigDO = databaseConfigMapper.getDataById(databaseConfigId);
            Map<String, Object> tableInfoMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
            tableInfoMap.put("id", dataBaseConfigDO.getTableId());
            tableInfoMap.put("text", dataBaseConfigDO.getTableName());
            result.add(tableInfoMap);
        } else if (Constants.XML_COLUMN_TYPE_DATALIST.equals(labelType)) {
            List<DataBaseRelationDO> dataBaseRelationDOList = databaseRelationMapper.listByDatabaseconfigId(databaseConfigId);
            if (dataBaseRelationDOList != null && dataBaseRelationDOList.size() > 0) {
                Map<String, Object> tableInfoMap;
                for (DataBaseRelationDO dataBaseRelationDO : dataBaseRelationDOList) {
                    tableInfoMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
                    tableInfoMap.put("id", dataBaseRelationDO.getTableId());
                    tableInfoMap.put("text", dataBaseRelationDO.getTableName());
                    result.add(tableInfoMap);
                }
            }
        }else{
            result=databaseConfigMapper.listTablesByDatabaseConfigId(databaseConfigId);
        }
        return result;
    }

    private JSONArray getBaseLabelType() {
        JSONArray result = new JSONArray();
        result.add(putLabelType(Constants.XML_COLUMN_TYPE_TXT));
        result.add(putLabelType(Constants.XML_COLUMN_TYPE_SUBTYPE));
        result.add(putLabelType(Constants.XML_COLUMN_TYPE_YYYYMMDDHHMMSS));
        result.add(putLabelType(Constants.XML_COLUMN_TYPE_SERIAL));
        result.add(putLabelType(Constants.XML_COLUMN_TYPE_STATISTICS));
        result.add(putLabelType(Constants.XML_COLUMN_TYPE_ROOTDATALIST));
        result.add(putLabelType(Constants.XML_COLUMN_TYPE_DATALIST));
        result.add(putLabelType(Constants.XML_COLUMN_TYPE_DATAITEM));
        result.add(putLabelType(Constants.XML_COLUMN_TYPE_DBCOLUMN));
        result.add(putLabelType(Constants.XML_COLUMN_TYPE_OTHER));
        return result;
    }

    private JSONObject putLabelType(String value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", value);
        jsonObject.put("text", value);
        jsonObject.put("enabled", false);
        return jsonObject;
    }

    private void setLabelEnabled(JSONArray jsonArray, String value) {
        for (Object o : jsonArray) {
            JSONObject jsonObject = (JSONObject) o;
            if (jsonObject.containsValue(value)) {
                jsonObject.put("enabled", true);
            }
        }
    }
}
