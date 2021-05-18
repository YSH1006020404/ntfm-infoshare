package cn.les.ntfm.infoshare.dao;

import cn.les.ntfm.infoshare.entity.ActiveMqConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * activemq配置Mapper
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-22 10:44
 */
@Mapper
public interface ActivemqConfigMapper {
    void addData(ActiveMqConfigDO activeMqConfigDO);

    void updateById(ActiveMqConfigDO activeMqConfigDO);

    ActiveMqConfigDO getDataById(@Param("id") Long id);
}
