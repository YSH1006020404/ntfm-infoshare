package cn.les.ntfm.infoshareConf.service;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 通用service
 *
 * @author 杨硕
 * @date 2020-06-10-上午10:37
 */
public interface CommonService {
    /**
     * 根据表的字典项ID获取该表的所有字段
     *
     * @param tableId 表的字典项ID
     * @return list
     */
    List<Map<String, String>> listColumns(Long tableId);

    /**
     * 获取所有可输出数据表
     *
     * @return list
     */
    List<Map<Long, String>> listOutputTable();
}
