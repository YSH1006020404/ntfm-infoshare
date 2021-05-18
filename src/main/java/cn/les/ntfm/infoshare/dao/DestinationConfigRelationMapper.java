package cn.les.ntfm.infoshare.dao;

import cn.les.ntfm.infoshare.entity.DestinationConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息目的地关联Mapper
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-22 11:43
 */
@Mapper
public interface DestinationConfigRelationMapper {
    void addData(DestinationConfigDO destinationConfigDO);

    void updateById(DestinationConfigDO destinationConfigDO);

    void deleteDataById(@Param("id") Long id);

    List<DestinationConfigDO> listData(DestinationConfigDO destinationConfigDO);
}
