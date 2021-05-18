package cn.les.ntfm.infoshare.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface SerialNumberMapper {

    /**
     * 数据检索
     *
     * @param linkid 检索内容
     * @return 检索结果
     */
    Map<String, Object> queryData(@Param("linkid") Long linkid);


    /**
     * 插入流水号数据
     *
     * @param map
     */
    void insertData(Map<String, Object> map);

    /**
     * @param map
     */
    void updateData(Map<String, Object> map);

    /**
     * 主键删除
     *
     * @param id
     */
    void deleteDataById(Long id);
}
