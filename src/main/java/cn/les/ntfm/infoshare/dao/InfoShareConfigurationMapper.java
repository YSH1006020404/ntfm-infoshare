package cn.les.ntfm.infoshare.dao;

import cn.les.ntfm.infoshare.entity.InfoshareConfigDO;
import cn.les.ntfm.infoshare.dto.MonitorLinkStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 信息共享配置Mapper
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-03-13 15:22
 */
@Mapper
public interface InfoShareConfigurationMapper {
    /**
     * 新增
     *
     * @param infoshareConfigDO
     */
    void addData(InfoshareConfigDO infoshareConfigDO);

    /**
     * 主键修改
     *
     * @param infoshareConfigDO
     */
    void updateDataById(InfoshareConfigDO infoshareConfigDO);

    /**
     * 主键删除
     *
     * @param id 主键
     */
    void deleteDataById(Long id);

    /**
     * 主键检索
     *
     * @param id
     * @return cn.les.ntfm.infoshare.entity.InfoshareConfigDO
     */
    InfoshareConfigDO getDataById(Long id);

    /**
     * 查询所有配置信息
     *
     * @return java.util.List<cn.les.ntfm.infoshare.entity.InfoshareConfigDO>
     */
    List<InfoshareConfigDO> listInfoShareConfigurations(InfoshareConfigDO infoShareConfiguration);

    /**
     * 首页链路列表检索
     *
     * @param map
     * @return
     */
    List<Map<String, Object>> listMsgs(Map<String, Object> map);

    /**
     * 检索逻辑链路状态信息
     *
     * @return
     */
    List<MonitorLinkStatus> listMonitorLinks(@Param(value = "currentTime") String currentTime);
}
