package cn.les.ntfm.infoshare.dao;

import cn.les.ntfm.infoshare.entity.IbmMqConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ibmmq配置Mapper
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-22 10:44
 */
@Mapper
public interface IbmmqConfigMapper {
    void addData(IbmMqConfigDO ibmMqConfigDO);

    void updateById(IbmMqConfigDO ibmMqConfigDO);

    IbmMqConfigDO getDataById(@Param("id") Long id);
}
