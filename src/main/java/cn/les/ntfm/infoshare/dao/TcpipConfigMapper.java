package cn.les.ntfm.infoshare.dao;

import cn.les.ntfm.infoshare.entity.TcpipConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * tcpip配置Mapper
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-22 10:44
 */
@Mapper
public interface TcpipConfigMapper {
    void addData(TcpipConfigDO tcpipConfigDO);

    void updateById(TcpipConfigDO tcpipConfigDO);

    TcpipConfigDO getDataById(@Param("id") Long id);
}
