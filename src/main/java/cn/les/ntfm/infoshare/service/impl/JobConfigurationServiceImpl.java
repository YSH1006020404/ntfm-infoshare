package cn.les.ntfm.infoshare.service.impl;

import cn.les.framework.core.util.ExceptionUtil;
import cn.les.ntfm.constant.ConfigConstants;
import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.entity.*;
import cn.les.ntfm.infoshare.dao.*;
import cn.les.ntfm.infoshare.dto.JobConfiguration;
import cn.les.ntfm.infoshare.quartz.job.BaseJob;
import cn.les.ntfm.infoshare.quartz.scheduler.ScheduleManager;
import cn.les.ntfm.infoshare.service.JobConfigurationService;
import cn.les.ntfm.util.FileUtil;
import cn.les.ntfm.util.FreemarkerUtil;
import cn.les.ntfm.util.Log4jUtils;
import com.github.pagehelper.util.StringUtil;
import com.google.common.base.CaseFormat;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * job配置ServiceImpl
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-04-29 15:18
 */
@Service(value = "jobConfigurationService")
public class JobConfigurationServiceImpl implements JobConfigurationService {
    @Resource
    private InfoShareConfigMapper infoShareConfigMapper;
    @Resource
    private ConfigConstants configConstants;
    @Resource
    private DatabaseConfigMapper databaseConfigMapper;
    @Resource
    private DatabaseRelationMapper databaseRelationMapper;
    @Resource
    private OutputRelationMapper outputRelationMapper;
    @Resource
    private CommonMapper commonMapper;
    @Resource
    private DestinationConfigRelationMapper destinationConfigRelationMapper;
    @Resource
    private XmlFormatConfigMapper xmlFormatConfigMapper;
    @Resource
    private PropertyDictMapper propertyDictMapper;
    @Resource
    private JobRunDetailMapper jobRunDetailMapper;
    @Resource
    private ScheduleManager scheduleManager;
    @Resource
    private BaseJob baseJob;


    /**
     * 刪除job
     *
     * @param linkId 链路ID
     */
    @Override
    public void delJob(Long linkId) {
        try {
            JobConfiguration job = new JobConfiguration();
            job.setJobGroup(String.valueOf(linkId));
            //删除全量任务
            job.setJobName(Constants.SEND_TYPE_ALL);
            scheduleManager.removeJob(job);
            //删除增量任务
            //删除JOBRUNDETAIL
            jobRunDetailMapper.removeJobFromTable(linkId);
            job.setJobName(Constants.SEND_TYPE_INC);
            scheduleManager.removeJob(job);

            //删除心跳任务
            job.setJobName(Constants.SEND_TYPE_HEARTBEAT);
            scheduleManager.removeJob(job);

            //删除ftl文件
            FileUtil.deleteFile(configConstants.getFtlPath(), linkId + Constants.FILE_FTL_SUFFIX);
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("JobConfigurationServiceImpl类的delJob方法出现错误，错误原因：", e);
        }
    }

    /**
     * job执行
     *
     * @param infoshareConfigDO 链路配置
     */
    @Override
    public void doJob(InfoshareConfigDO infoshareConfigDO) {
        try {
            List<JobConfiguration> jobConfigurationList = getJobConfigurationList(infoshareConfigDO);
            if (jobConfigurationList != null) {
                for (JobConfiguration job : jobConfigurationList) {
                    if (StringUtil.isNotEmpty(job.getFrequency())) {
                        scheduleManager.startJob(job);
                    }
                }
            }
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("JobConfigurationServiceImpl类的doJob方法出现错误，错误原因：", e);
        }
    }

    @Override
    public void triggerAll(Long infoshareId) throws Exception {
        InfoshareConfigDO configuration = infoShareConfigMapper.getDataById(infoshareId);
        List<JobConfiguration> jobConfigurationList = getJobConfigurationList(configuration);
        if (jobConfigurationList != null && jobConfigurationList.size() > 0) {
            for (JobConfiguration job : jobConfigurationList) {
                if (Constants.SEND_TYPE_ALL.equals(job.getJobName())) {
                    baseJob.process(job);
                }
            }
        }
    }

    /**
     * 新增job
     *
     * @param linkId 链路配置ID
     */
    @Override
    public void addJob(Long linkId) {
        try {
            List<JobConfiguration> jobConfigurationList = getJobConfigurationList(infoShareConfigMapper.getDataById(linkId));
            if (jobConfigurationList != null && jobConfigurationList.size() > 0) {
                for (JobConfiguration job : jobConfigurationList) {
                    if (StringUtil.isNotEmpty(job.getFrequency())) {
                        scheduleManager.startJob(job);
                    }
                }
            }
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("JobConfigurationServiceImpl类的addJob方法出现错误，错误原因：", e);
        }
    }

    /**
     * 获取指定链路的所有定时任务配置
     *
     * @param infoshareConfigDO 链路配置
     * @return java.util.List<cn.les.ntfm.infoshare.dto.JobConfiguration>
     */
    private List<JobConfiguration> getJobConfigurationList(InfoshareConfigDO infoshareConfigDO) throws Exception {
        //链路关闭
        if (!infoshareConfigDO.getStateFlag()) {
            return null;
        }
        JobConfiguration jobConfiguration = new JobConfiguration();
        //链路唯一标识
        jobConfiguration.setLinkID(infoshareConfigDO.getId());
        //交互标识
        jobConfiguration.setInteractionMark(infoshareConfigDO.getInteractionMark());
        //job组
        jobConfiguration.setJobGroup(String.valueOf(infoshareConfigDO.getId()));

        //数据源配置
        DataBaseConfigDO dataBaseConfig = databaseConfigMapper.getDataById(infoshareConfigDO.getSourceId());
        if (dataBaseConfig == null) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .warn("链路" + infoshareConfigDO.getId() + "没有配置数据源！");
            return null;
        }
        //报文格式配置
        XmlFormatConfigDO xmlFormatConfigDO = new XmlFormatConfigDO();
        xmlFormatConfigDO.setId(dataBaseConfig.getXmlformatconfigId());
        jobConfiguration.setXmlFormatConfigList(xmlFormatConfigMapper.listXmlFormatConfig(xmlFormatConfigDO));
        //每条报文封装的数据量
        jobConfiguration.setSplitNum(dataBaseConfig.getSplitNum());
        //表名
        jobConfiguration.setTableName(dataBaseConfig.getTableName());
        //增量发送是否去重的开关
        jobConfiguration.setDeduplicationFlag(dataBaseConfig.getDeduplicationFlag());
        //脚本设置
        setJobConfigurationSqls(jobConfiguration, dataBaseConfig);
        //freemarker模板
        FreemarkerUtil.createFtlFiles(jobConfiguration, dataBaseConfig.getXmlformatconfigId());
        //输出目的地配置
        Map<Long, Date> destinationUpdateTime = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
        Map<Long, String> sendTypeServiceMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
        Map<Long, HashMap<String, Object>> destinationConfigMaps = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
        DestinationConfigDO destinationParam = new DestinationConfigDO();
        destinationParam.setInfoshareconfigId(infoshareConfigDO.getId());
        List<DestinationConfigDO> destinationConfigList = destinationConfigRelationMapper.listData(destinationParam);
        if (destinationConfigList != null && destinationConfigList.size() > 0) {
            for (DestinationConfigDO destinationConfigDO : destinationConfigList) {
                //筛选出输出方式配置状态为启动的配置
                if (destinationConfigDO.getStatusFlag()) {
                    //job发送数据目的地的配置详情
                    HashMap<String, Object> destinationConfigMap = commonMapper
                            .getConfigurationById(destinationConfigDO.getDestinationType(),
                                    destinationConfigDO.getDestinationId());
                    destinationConfigMaps.put(destinationConfigDO.getId(), destinationConfigMap);

                    //数据发送实例
                    //获取输出方式的配置信息
                    PropertyDictDO destinationType = propertyDictMapper.getDataById(destinationConfigDO.getDestinationTypeId());
                    if (destinationType == null) {
                        Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                                .warn("链路" + infoshareConfigDO.getId() + "没有输出方式" + destinationConfigDO.getDestinationTypeId());
                        continue;
                    }
                    sendTypeServiceMap.put(destinationConfigDO.getId(), destinationType.getDictValue());
                    destinationUpdateTime.put(destinationConfigDO.getId(), (Date) destinationConfigMap.get("updateTime"));
                }
            }
        }
        jobConfiguration.setDestinationUpdateTime(destinationUpdateTime);
        jobConfiguration.setDestinationConfigMaps(destinationConfigMaps);
        jobConfiguration.setSendTypeServiceMap(sendTypeServiceMap);

        //job结果
        List<JobConfiguration> result = new ArrayList<>();
        //全量发送job
        //job名称
        jobConfiguration.setJobName(Constants.SEND_TYPE_ALL);
        //job执行频率
        jobConfiguration.setFrequency(dataBaseConfig.getAllFrequency());
        result.add(jobConfiguration);

        //增量发送job
        JobConfiguration incJobConfiguration = new JobConfiguration();
        BeanUtils.copyProperties(jobConfiguration, incJobConfiguration);
        //job名称
        incJobConfiguration.setJobName(Constants.SEND_TYPE_INC);
        //job执行频率
        incJobConfiguration.setFrequency(dataBaseConfig.getIncFrequency());
        result.add(incJobConfiguration);

        //心跳信息发送job
        if (dataBaseConfig.getBeatFlag()) {
            JobConfiguration heartBeatJobConfiguration = new JobConfiguration();
            BeanUtils.copyProperties(jobConfiguration, heartBeatJobConfiguration);
            //job名称
            heartBeatJobConfiguration.setJobName(Constants.SEND_TYPE_HEARTBEAT);
            //job执行频率
            heartBeatJobConfiguration.setFrequency(configConstants.getHeartBeatFrequncy());
            result.add(heartBeatJobConfiguration);
        }
        //获取该链路涉及的job
        if (result.size() == 0) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .warn("链路" + infoshareConfigDO.getId() + "没有生成相应job！");
        }
        return result;
    }

    /**
     * 设置检索和更新脚本
     *
     * @param jobConfiguration job配置信息
     * @param dataBaseConfig   数据输出信息
     */
    private void setJobConfigurationSqls(JobConfiguration jobConfiguration, DataBaseConfigDO dataBaseConfig) {
        //获取关联表配置字段项
        List<DataBaseRelationDO> dataBaseRelationList = databaseRelationMapper.listByDatabaseconfigId(dataBaseConfig.getId());
        if (dataBaseRelationList == null || dataBaseRelationList.size() == 0) {
            jobConfiguration.setHasRelationTables(false);
        } else {
            dataBaseConfig.setDataBaseRelationList(dataBaseRelationList);
            Map<Long, String> relationTableSelectSqlAllMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
            Map<Long, String> relationTableDataLabelMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
            Map<Long, DataBaseRelationDO> relationTableMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
            Map<Long, List<String>> relationTableClobColumns = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
            for (DataBaseRelationDO dataBaseRelationDO : dataBaseRelationList) {
                List<OutputRelationDO> outputRelationList = outputRelationMapper.listRelationTableData(dataBaseRelationDO.getTableId(), dataBaseConfig.getXmlformatconfigId());
                if (outputRelationList == null || outputRelationList.size() == 0) {
                    return;
                }

                //基础表的CLOB字段
                relationTableClobColumns.put(dataBaseRelationDO.getId(), setClobColumns(outputRelationList));

                String selectSql = getSelectSql(outputRelationList, dataBaseConfig, dataBaseRelationDO, Constants.SEND_TYPE_ALL);
                //关联表的XMLLIST配置
                XmlFormatConfigDO relationDataListParam = new XmlFormatConfigDO();
                relationDataListParam.setId(dataBaseConfig.getXmlformatconfigId());
                relationDataListParam.setLabelType(Constants.XML_COLUMN_TYPE_DATALIST);
                relationDataListParam.setTableId(dataBaseRelationDO.getTableId());
                List<XmlFormatConfigDO> relationDataListXmlFormatConfig = xmlFormatConfigMapper.listXmlFormatConfig(relationDataListParam);
                if (relationDataListXmlFormatConfig == null || relationDataListXmlFormatConfig.size() != 1) {
                    ExceptionUtil.throwBusinessEx("没有检索到关联表的XML配置！");
                    continue;
                }
                Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                        .warn("链路" + jobConfiguration.getLinkID() + "的关联表" + dataBaseRelationDO.getTableName() + "检索脚本："
                                + selectSql);
                relationTableSelectSqlAllMap.put(dataBaseRelationDO.getId(), selectSql);
                relationTableDataLabelMap.put(dataBaseRelationDO.getId(), relationDataListXmlFormatConfig.get(0).getLabelName());
                relationTableMap.put(dataBaseRelationDO.getId(), dataBaseRelationDO);
            }
            //基础表的CLOB字段
            jobConfiguration.setRelationTableClobColumns(relationTableClobColumns);
            //关联表检索脚本
            jobConfiguration.setRelationTableSelectSqlAllMap(relationTableSelectSqlAllMap);
            //关联表XML集合标签
            jobConfiguration.setRelationTableDataLabelMap(relationTableDataLabelMap);
            //关联表详情
            jobConfiguration.setRelationTableMap(relationTableMap);
            //是否有关联表
            jobConfiguration.setHasRelationTables(true);
        }
        //获取基础表配置字段项
        List<OutputRelationDO> outputRelationList = outputRelationMapper.listRelationTableData(dataBaseConfig.getTableId(), dataBaseConfig.getXmlformatconfigId());
        if (outputRelationList == null || outputRelationList.size() == 0) {
            return;
        }
        //获取关联表在基础表中的更新时间字段
        String[] relationTableColumns = getRelationTableColumns(dataBaseConfig);
        String[] primaryKeys = getPrimaryKeys(dataBaseConfig);
        if (relationTableColumns != null && relationTableColumns.length > 0) {
            Boolean existFlag;
            for (String relationTableColumn : relationTableColumns) {
                existFlag = false;
                for (OutputRelationDO outputRelationDO : outputRelationList) {
                    if (relationTableColumn.equals(outputRelationDO.getColumnName())) {
                        existFlag = true;
                        break;
                    }
                }
                if (!(existFlag || ArrayUtils.contains(primaryKeys, relationTableColumn))) {
                    OutputRelationDO outputRelationRelation = new OutputRelationDO();
                    outputRelationRelation.setColumnName(relationTableColumn);
                    if (relationTableColumn.endsWith(configConstants.getUpdateTimeColumn())) {
                        outputRelationRelation.setColumnType(Constants.DATE_TYPE_DATE);
                        outputRelationRelation.setDateFormat(Constants.DATE_YYYYMMDDHHMMSS);
                    }
                    outputRelationList.add(outputRelationRelation);
                }
            }
        }
        //基础表的CLOB字段
        jobConfiguration.setBasetableClobColumns(setClobColumns(outputRelationList));

        //基础表全量检索脚本
        String baseTableSelectSqlAll = getSelectSql(outputRelationList, dataBaseConfig, null, Constants.SEND_TYPE_ALL);
        jobConfiguration.setBaseTableSelectSqlAll(baseTableSelectSqlAll);
        Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                .warn("链路" + jobConfiguration.getLinkID() + "的基础表全量检索脚本：" + baseTableSelectSqlAll);
        //基础表增量检索脚本
        String baseTableSelectSqlInc = getSelectSql(outputRelationList, dataBaseConfig, null, Constants.SEND_TYPE_INC);
        jobConfiguration.setBaseTableSelectSqlInc(baseTableSelectSqlInc);
        Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                .warn("链路" + jobConfiguration.getLinkID() + "的基础表增量检索脚本：" + baseTableSelectSqlInc);
        if (jobConfiguration.getDeduplicationFlag()) {
            //去重表数据更新脚本
            String mergeSql = getMergeSql(outputRelationList, dataBaseConfig);
            jobConfiguration.setMergeSql(mergeSql);
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .warn("链路" + jobConfiguration.getLinkID() + "的去重表数据更新脚本：" + mergeSql);
        }
    }

    /**
     * 获取所有CLOB字段
     */
    private List<String> setClobColumns(List<OutputRelationDO> outputRelationList) {
        List<String> result = new ArrayList<>();
        for (OutputRelationDO outputRelationDO : outputRelationList) {
            if (Constants.DATE_TYPE_CLOB.equals(outputRelationDO.getColumnType())) {
                result.add(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, outputRelationDO.getColumnName()));
            }
        }
        return result;
    }

    /**
     * 检索脚本生成
     *
     * @param outputRelationList 输出字段
     * @param dataBaseConfigDO   基础表配置
     * @param dataBaseRelationDO 关联表配置
     * @param sendType           发送类型（增量/全量）
     * @return 检索脚本
     */
    private String getSelectSql(List<OutputRelationDO> outputRelationList, DataBaseConfigDO dataBaseConfigDO, DataBaseRelationDO dataBaseRelationDO, String sendType) {
        StringBuilder selectSql = new StringBuilder(Constants.SQL_SELECT);
        for (OutputRelationDO outputRelationDO : outputRelationList) {
            //DATE类型
            if (Constants.DATE_TYPE_DATE.equals(outputRelationDO.getColumnType())) {
                selectSql.append(Constants.SQL_OPEN_PARENTHESIS);
                selectSql.append(Constants.SQL_TO_CHAR).append(Constants.SQL_OPEN_PARENTHESIS);
                selectSql.append(outputRelationDO.getColumnName());
                //转UTC时
                if (configConstants.getTransferToUTC()) {
                    selectSql.append(Constants.SQL_TO_UTC);
                }
                selectSql.append(Constants.COMMA);
                selectSql.append(Constants.SINGLE_QUOTES).append(outputRelationDO.getDateFormat()).append(Constants.SINGLE_QUOTES);
                selectSql.append(Constants.SQL_CLOSE_PARENTHESIS);
                selectSql.append(Constants.SQL_CLOSE_PARENTHESIS);
                selectSql.append(Constants.SQL_AS);
                selectSql.append(getColumnName(outputRelationDO.getColumnName()));
            } else if (Constants.DATE_TYPE_CLOB.equals(outputRelationDO.getColumnType())) {
                //CLOB处理
                selectSql.append(getColumnName(outputRelationDO.getColumnName()));
            } else {
                selectSql.append(getColumnName(outputRelationDO.getColumnName()));
            }
            selectSql.append(Constants.COMMA);
        }

        if (dataBaseRelationDO == null) {
            //检索基础表的主键、关联字段等必须字段
            String[] requiredKeys = getRequiredKeys(getPrimaryKeys(dataBaseConfigDO), getRelationTableColumns(dataBaseConfigDO));
            if (requiredKeys != null && requiredKeys.length > 0) {
                setRequiredKeys(requiredKeys, outputRelationList, selectSql);
            }
            selectSql.append(configConstants.getUpdateTimeColumn());
            selectSql.append(Constants.SQL_AS);
            selectSql.append(Constants.SQL_UPDATE_TIME_COLUMN);
            selectSql.append(Constants.SQL_FROM);

            //调试 TODO
            //基础表的信息检索
            if (Constants.SEND_TYPE_INC.equals(sendType) && dataBaseConfigDO.getDeduplicationFlag()) {
                selectSql.append(Constants.SQL_TABLE_C_PREFIX).append(dataBaseConfigDO.getTableName());
                selectSql.append(Constants.SQL_WHERE);
                selectSql.append("LINKID = #{linkid}");
            } else {
                selectSql.append(dataBaseConfigDO.getTableName());
                selectSql.append(Constants.SQL_WHERE);
                selectSql.append("1=1");
            }
            if (StringUtils.isNotEmpty(dataBaseConfigDO.getCondition())) {
                selectSql.append(Constants.SQL_AND);
                selectSql.append(dataBaseConfigDO.getCondition());
            }
            if (Constants.SEND_TYPE_INC.equals(sendType)) {
                selectSql.append(Constants.SQL_AND);
                if (Constants.DATE_TYPE_DATE.equals(configConstants.getDateType())) {
                    selectSql.append(configConstants.getUpdateTimeColumn());
                    selectSql.append(Constants.SQL_GREATER_THAN_SIGN);
                    selectSql.append(Constants.SQL_UPDATE_TIME_PARAM);
                } else {
                    selectSql.append(configConstants.getUpdateTimeColumn());
                    selectSql.append(Constants.SQL_GREATER_THAN_SIGN);
                    selectSql.append(Constants.SQL_TO_CHAR).append(Constants.SQL_OPEN_PARENTHESIS).append(Constants.SQL_UPDATE_TIME_PARAM);
                    selectSql.append(Constants.COMMA).append(Constants.YYYYMMDDHH24MISS).append(Constants.SQL_CLOSE_PARENTHESIS);
                }
            }
            /*//基础表的信息检索
            if (Constants.SEND_TYPE_INC.equals(sendType) && dataBaseConfigDO.getDeduplicationFlag()) {
                selectSql.append(Constants.SQL_TABLE_C_PREFIX).append(dataBaseConfigDO.getTableName());
            } else {
                selectSql.append(dataBaseConfigDO.getTableName());
            }
            if (StringUtils.isNotEmpty(dataBaseConfigDO.getCondition())) {
                selectSql.append(Constants.SQL_WHERE);
                selectSql.append(dataBaseConfigDO.getCondition());
            }
            if (Constants.SEND_TYPE_INC.equals(sendType)) {
                if (StringUtils.isEmpty(dataBaseConfigDO.getCondition())) {
                    selectSql.append(Constants.SQL_WHERE);
                } else {
                    selectSql.append(Constants.SQL_AND);
                }
                if (Constants.DATE_TYPE_DATE.equals(configConstants.getDateType())) {
                    selectSql.append(configConstants.getUpdateTimeColumn());
                    selectSql.append(Constants.SQL_GREATER_THAN_SIGN);
                    selectSql.append(Constants.SQL_UPDATE_TIME_PARAM);
                } else {
                    selectSql.append(configConstants.getUpdateTimeColumn());
                    selectSql.append(Constants.SQL_GREATER_THAN_SIGN);
                    selectSql.append(Constants.SQL_TO_CHAR).append(Constants.SQL_OPEN_PARENTHESIS).append(Constants.SQL_UPDATE_TIME_PARAM);
                    selectSql.append(Constants.COMMA).append(Constants.YYYYMMDDHH24MISS).append(Constants.SQL_CLOSE_PARENTHESIS);
                }
            }*/
        } else {
            selectSql.append(configConstants.getUpdateTimeColumn());
            selectSql.append(Constants.SQL_AS);
            selectSql.append(Constants.SQL_UPDATE_TIME_COLUMN);
            selectSql.append(Constants.SQL_FROM);
            //关联表的信息检索
            selectSql.append(dataBaseRelationDO.getTableName());
            selectSql.append(Constants.SQL_WHERE);
            //关联表的关联字段条件
            selectSql.append(dataBaseRelationDO.getRelationTableColumn());
            selectSql.append(Constants.SQL_EQUALS);
            selectSql.append(Constants.SQL_HASH).append(Constants.SQL_OPEN_BRACE)
                    .append(Constants.SQL_RELATION_COLUMN_PARAM)
                    .append(Constants.SQL_CLOSE_BRACE);
            //关联表的筛选条件
            if (StringUtils.isNotEmpty(dataBaseRelationDO.getCondition())) {
                selectSql.append(Constants.SQL_AND).append(dataBaseRelationDO.getCondition());
            }
        }
        //排序
        selectSql.append(Constants.SQL_ORDER_BY);
        selectSql.append(configConstants.getUpdateTimeColumn());
        selectSql.append(Constants.SQL_ORDER_DESC);
        return selectSql.toString();
    }

    /**
     * 设置必须检索的字段
     *
     * @param requiredKeys       原必须字段
     * @param outputRelationList 字段配置列表
     * @param stringBuilder      检索脚本
     */
    private void setRequiredKeys(String[] requiredKeys, List<OutputRelationDO> outputRelationList, StringBuilder stringBuilder) {
        forRequiredKey:
        for (String requiredKey : requiredKeys) {
            if (StringUtil.isEmpty(requiredKey)) {
                continue;
            }
            for (OutputRelationDO outputRelation : outputRelationList) {
                //主键为输出配置项
                if (requiredKey.equals(outputRelation.getColumnName())) {
                    break forRequiredKey;
                }
            }
            stringBuilder.append(getColumnName(requiredKey));
            stringBuilder.append(Constants.COMMA);
        }
    }

    /**
     * 获取去重表脚本
     *
     * @param outputRelationList 输出字段
     * @param dataBaseConfig     数据源配置
     * @return 去重表脚本
     */
    private String getMergeSql(List<OutputRelationDO> outputRelationList, DataBaseConfigDO dataBaseConfig) {
        if (outputRelationList != null && outputRelationList.size() > 0) {
            StringBuilder mergeUsing = new StringBuilder("USING (SELECT ");
            StringBuilder mergeOn = new StringBuilder("(");
            StringBuilder mergeMatchedUpdate = new StringBuilder("UPDATE SET ");
            StringBuilder mergeMatchedUpdateWhere = new StringBuilder(" WHERE ");
            StringBuilder mergeNotMatchedInsert = new StringBuilder("INSERT (");
            StringBuilder mergeNotMatchedInsertValue = new StringBuilder(" VALUES (");
            String[] primaryKeys = getPrimaryKeys(dataBaseConfig);
            //基础表字段
            List<Map<String, String>> baseTableColumns = commonMapper.listColumns(dataBaseConfig.getTableName());
            for (Map<String, String> baseTableColumn : baseTableColumns) {
                String columnName = getColumnName(baseTableColumn.get("id"));
                mergeUsing.append(columnName);
                mergeUsing.append(Constants.COMMA);
                if (!ArrayUtils.contains(primaryKeys, baseTableColumn.get("id"))) {
                    mergeMatchedUpdate.append(" T1.").append(columnName);
                    mergeMatchedUpdate.append(" = T2.").append(columnName);
                    mergeMatchedUpdate.append(Constants.COMMA);
                }
                mergeNotMatchedInsert.append(columnName);
                mergeNotMatchedInsert.append(Constants.COMMA);

                mergeNotMatchedInsertValue.append("T2.").append(columnName);
                mergeNotMatchedInsertValue.append(Constants.COMMA);
            }

            for (OutputRelationDO outputRelation : outputRelationList) {
                if (Constants.DATE_TYPE_DATE.equals(outputRelation.getColumnType())) {
                    mergeMatchedUpdateWhere.append("NVL(TO_CHAR(T1.").append(getColumnName(outputRelation.getColumnName())).append("," + Constants.YYYYMMDDHH24MISSSSS + "), 0) ");
                    mergeMatchedUpdateWhere.append("!= NVL(TO_CHAR(T2.").append(getColumnName(outputRelation.getColumnName())).append("," + Constants.YYYYMMDDHH24MISSSSS + "), 0) ");
                } else {
                    mergeMatchedUpdateWhere.append("NVL(TO_CHAR(T1.").append(getColumnName(outputRelation.getColumnName())).append("), 0) ");
                    mergeMatchedUpdateWhere.append("!= NVL(TO_CHAR(T2.").append(getColumnName(outputRelation.getColumnName())).append("), 0)");
                }
                mergeMatchedUpdateWhere.append(Constants.SQL_OR);
            }
            for (String primaryKey : primaryKeys) {
                if (StringUtil.isEmpty(primaryKey)) {
                    continue;
                }
                mergeOn.append("NVL(TO_CHAR(T1.").append(getColumnName(primaryKey)).append("), 0) ").append("= NVL(TO_CHAR(T2.").append(getColumnName(primaryKey)).append("), 0) ");
                mergeOn.append(Constants.SQL_AND);
            }

            mergeUsing.deleteCharAt(mergeUsing.lastIndexOf(Constants.COMMA));
            mergeUsing.append(" FROM ").append(dataBaseConfig.getTableName());
            mergeUsing.append(Constants.SQL_WHERE);
            if (!StringUtil.isEmpty(dataBaseConfig.getCondition())) {
                mergeUsing.append(dataBaseConfig.getCondition());
                mergeUsing.append(Constants.SQL_AND);
            }
            if (Constants.DATE_TYPE_DATE.equals(configConstants.getDateType())) {
                mergeUsing.append(configConstants.getUpdateTimeColumn())
                        .append(Constants.SQL_GREATER_THAN_SIGN)
                        .append("#{time}");
            } else {
                mergeUsing.append(configConstants.getUpdateTimeColumn())
                        .append(Constants.SQL_GREATER_THAN_SIGN)
                        .append(Constants.SQL_TO_CHAR)
                        .append(Constants.SQL_OPEN_PARENTHESIS)
                        .append("#{time}")
                        .append(Constants.COMMA)
                        .append(Constants.YYYYMMDDHH24MISS)
                        .append(Constants.SQL_CLOSE_PARENTHESIS);
            }
            mergeUsing.append(") T2");

            mergeOn.append("NVL(TO_CHAR(T1.").append(Constants.SQL_LINKID).append("), 0) ");
            mergeOn.append(" = #{").append(Constants.SQL_LINKID).append("} )");

            mergeMatchedUpdate.deleteCharAt(mergeMatchedUpdate.lastIndexOf(Constants.COMMA));
            mergeMatchedUpdateWhere.delete(
                    mergeMatchedUpdateWhere.length() - Constants.SQL_OR.length()
                    , mergeMatchedUpdateWhere.length());
            mergeMatchedUpdate.append(mergeMatchedUpdateWhere);

            mergeNotMatchedInsert.append(Constants.SQL_LINKID);
            mergeNotMatchedInsert.append(")");

            mergeNotMatchedInsertValue.append("#{").append(Constants.SQL_LINKID).append("}");
            mergeNotMatchedInsertValue.append(")");
            mergeNotMatchedInsert.append(mergeNotMatchedInsertValue);

            //数据更新脚本
            String mergeSql = Constants.SQL_MERGE_INTO + Constants.SQL_TABLE_C_PREFIX + dataBaseConfig.getTableName() + " T1 "
                    + mergeUsing.toString()
                    + Constants.SQL_ON
                    + mergeOn.toString()
                    + Constants.SQL_MATCHED
                    + mergeMatchedUpdate.toString()
                    + Constants.SQL_NOT_MATCHED
                    + mergeNotMatchedInsert.toString();
            return mergeSql;
        } else {
            return null;
        }
    }

    /**
     * 检索数据库字段原名称（加引号可以保证不转换大小写）
     *
     * @param column 数据库字段名
     * @return String
     */
    private String getColumnName(String column) {
        return "\"" + column + "\"";
    }

    /**
     * 获取数据源表的必要信息
     *
     * @param primaryKeys  基础表的主键信息
     * @param relationKeys 关联表相关信息
     * @return String[]
     */
    private String[] getRequiredKeys(String[] primaryKeys, String[] relationKeys) {
        if (primaryKeys == null) {
            return null;
        } else {
            if (relationKeys == null) {
                return primaryKeys;
            } else {
                String[] requiredKeys = new String[primaryKeys.length + relationKeys.length];
                System.arraycopy(primaryKeys, 0, requiredKeys, 0, primaryKeys.length);
                System.arraycopy(relationKeys, 0, requiredKeys, primaryKeys.length, relationKeys.length);
                return requiredKeys;
            }
        }
    }

    /**
     * 获取基础表的主键信息
     *
     * @param dataBaseConfigDO 数据源配置
     * @return String[]
     */
    private String[] getPrimaryKeys(DataBaseConfigDO dataBaseConfigDO) {
        PropertyDictDO outputTable = propertyDictMapper.getDataById(dataBaseConfigDO.getTableId());
        if (StringUtil.isEmpty(outputTable.getColumnLabel())) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .warn(outputTable.getRelationTable() + "表没有配置主键信息！");
            return null;
        } else {
            return outputTable.getColumnLabel().split(Constants.COMMA);
        }
    }

    /**
     * 获取关联表相关信息
     *
     * @param dataBaseConfigDO 数据源配置
     * @return String[]
     */
    private String[] getRelationTableColumns(DataBaseConfigDO dataBaseConfigDO) {
        if (dataBaseConfigDO.getDataBaseRelationList() != null && dataBaseConfigDO.getDataBaseRelationList().size() > 0) {
            int length = dataBaseConfigDO.getDataBaseRelationList().size();
            String[] relationKeys = new String[2 * length];
            for (int i = 0; i < length; i++) {
                //基础表的关联字段
                relationKeys[2 * i] = dataBaseConfigDO.getDataBaseRelationList().get(i).getBaseTableColumn();
                //基础表的关联表更新时间
                relationKeys[2 * i + 1] = dataBaseConfigDO.getDataBaseRelationList().get(i).getTableName()
                        + Constants.UNDERLINE + configConstants.getUpdateTimeColumn();
            }
            return relationKeys;
        } else {
            return null;
        }
    }
}