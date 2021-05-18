package cn.les.ntfm.infoshare.dao;

import cn.les.ntfm.infoshare.entity.PropertyDictDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 字典项Mapper
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-03-14 15:35
 */
@Mapper
public interface PropertyDictMapper {
    /**
     * 根据字典项类型获取字典项值
     *
     * @param name
     * @return java.util.List<cn.les.ntfm.infoshare.entity.PropertyDictDO>
     */
    List<Map<Long, String>> listJsonDataByPropertyTypeName(String name);

    /**
     * 根据字典项类型名称和字典项名称获取字典项
     *
     * @param propertyName
     * @param displayName
     * @return cn.les.ntfm.infoshare.entity.PropertyDictDO
     */
    PropertyDictDO getPropertyDictByTypeNameAndDictName(@Param("propertyName") String propertyName, @Param("displayName") String displayName);

    /**
     * 主键检索
     *
     * @param id
     * @return cn.les.ntfm.infoshare.entity.PropertyDictDO
     */
    PropertyDictDO getDataById(Long id);

    /**
     * 唯一标识检索
     *
     * @param id
     * @return cn.les.ntfm.infoshare.entity.PropertyDictDO
     */
    PropertyDictDO queryColumnLabel(Long id);
}
