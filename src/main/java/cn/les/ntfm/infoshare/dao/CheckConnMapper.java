package cn.les.ntfm.infoshare.dao;

import cn.les.ntfm.infoshare.entity.CheckConnDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by jxh on2019/10/10
 */
@Mapper
public interface CheckConnMapper {
    /**
     * 查询实例列表
     * @return
     */
    List<CheckConnDo> queryInstances();

    /**
     * 查询本机实例
     * @param host
     * @return
     */
    CheckConnDo queryInstance(@Param("host") String host);


    /**
     * 初始化本机实例
     * @param host
     */
    void  initInstance(@Param("host") String host);


    /**
     * 根据连接情况修改conn字段和检查时间
     * @param host
     * @param inc
     */
    void updateCheckConn(@Param("host") String host, @Param("inc") int inc);

    /**
     * 根据数据库状态更新检查时间字段
     * @param host
     */
    void updateCheckTime(@Param("host") String host);

    void updateOper(@Param("host") String host,@Param("oper") String oper);


}
