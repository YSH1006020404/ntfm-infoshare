package cn.les.ntfm.infoshare.dao;

import cn.les.ntfm.infoshare.entity.JobRunDetailDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * job执行状况Mapper
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-24 16:09
 */
@Mapper
public interface JobRunDetailMapper {
    /**
     * 数据清除
     *
     * @return void
     */
    void truncateTable();

    /**
     * 数据添加
     *
     * @param jobRunDetail
     * @return void
     */
    void addJobRunDetail(JobRunDetailDO jobRunDetail);

    /**
     * 数据更新
     *
     * @param jobRunDetail
     * @return void
     */
    void updateJobRunDetailById(JobRunDetailDO jobRunDetail);

    /**
     * 条件检索
     *
     * @param jobRunDetail
     * @return cn.les.ntfm.infoshare.entity.JobRunDetailDO
     */
    JobRunDetailDO getJobRunDetail(JobRunDetailDO jobRunDetail);

    void removeJobFromTable(Long id);

}
