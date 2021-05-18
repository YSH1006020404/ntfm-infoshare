package cn.les.ntfm.infoshareConf.service.impl;

import cn.les.framework.web.base.impl.BaseServiceImpl;
import cn.les.ntfm.constant.PropertyNameConstants;
import cn.les.ntfm.infoshare.dao.CommonMapper;
import cn.les.ntfm.infoshare.dao.PropertyDictMapper;
import cn.les.ntfm.infoshareConf.service.CommonService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


@Service("commonService")
public class CommonServiceImpl extends BaseServiceImpl implements CommonService {
    @Resource
    private PropertyDictMapper propertyDictMapper;
    @Resource
    private CommonMapper commonMapper;

    @Override
    public List<Map<String, String>> listColumns(Long tableId) {
        String tableName = propertyDictMapper.getDataById(tableId).getRelationTable();
//        return commonMapper.listColumns(tableName);
        List<Map<String, String>> result = commonMapper.listColumns(tableName);
        //TODO
        /*StringBuilder sb = new StringBuilder("MERGE INTO ");
        sb.append(tableName);
        sb.append(" T1 \r\n");
        sb.append("USING (SELECT #{MDRSID} AS MDRSID FROM DUAL) T2 \r\n");
        sb.append("ON (T1.MDRSID = T2.MDRSID)\r\n");
        sb.append("WHEN MATCHED THEN \r\n");
        sb.append("UPDATE SET ");
        for (Map<String, String> stringStringMap : result) {
            sb.append(stringStringMap.get("id"));
            sb.append(" = ");
            if ("DATE".equals(stringStringMap.get("columnType"))) {
                sb.append("TO_DATE(#{");
                sb.append(stringStringMap.get("id"));
                sb.append("},'yyyymmddhh24miss'), ");
            } else {
                sb.append("#{");
                sb.append(stringStringMap.get("id"));
                sb.append("}, ");
            }
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("\r\n");
        sb.append("WHERE MDRSID = #{MDRSID}\r\n");
        sb.append("WHEN NOT MATCHED THEN \r\n");
        sb.append("INSERT (");
        for (Map<String, String> stringStringMap : result) {
            sb.append(stringStringMap.get("id"));
            sb.append(", ");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append(")\r\n");
        sb.append("VALUES (");
        for (Map<String, String> stringStringMap : result) {
            if ("DATE".equals(stringStringMap.get("columnType"))) {
                sb.append("TO_DATE(#{");
                sb.append(stringStringMap.get("id"));
                sb.append("},'yyyymmddhh24miss'), ");
            } else {
                sb.append("#{");
                sb.append(stringStringMap.get("id"));
                sb.append("}, ");
            }
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append(")");
        System.out.println(sb.toString());*/
        return result;
    }

    @Override
    public List<Map<Long, String>> listOutputTable() {
        return propertyDictMapper.listJsonDataByPropertyTypeName(PropertyNameConstants.SOURCE_TABLE_TYPE);
    }

}