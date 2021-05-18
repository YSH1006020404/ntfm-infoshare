package cn.les.ntfm.infoshare.dao;

import cn.les.ntfm.infoshare.entity.FtpConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * ftp配置Mapper
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-22 10:44
 */
@Mapper
public interface FtpConfigMapper {
    void addData(FtpConfigDO ftpConfigDO);

    void updateById(FtpConfigDO ftpConfigDO);

    FtpConfigDO getDataById(@Param("id") Long id);
}
