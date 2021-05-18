package cn.les.ntfm.infoshare.quartz.job;

import cn.les.framework.core.util.DateUtil;
import cn.les.ntfm.constant.ConfigConstants;
import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.entity.DataBaseRelationDO;
import cn.les.ntfm.infoshare.entity.JobRunDetailDO;
import cn.les.ntfm.infoshare.entity.XmlFormatConfigDO;
import cn.les.ntfm.infoshare.dao.CommonMapper;
import cn.les.ntfm.infoshare.dao.JobRunDetailMapper;
import cn.les.ntfm.infoshare.dto.JobConfiguration;
import cn.les.ntfm.infoshare.service.impl.LinkStatisticsServiceImpl;
import cn.les.ntfm.infoshare.service.impl.SendDataThread;
import cn.les.ntfm.util.DateUtils;
import cn.les.ntfm.util.FreemarkerUtil;
import cn.les.ntfm.util.Log4jUtils;
import cn.les.ntfm.util.NamedThreadFactory;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.util.StringUtil;
import com.google.common.base.CaseFormat;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 基础job功能
 *
 * @author 杨硕
 * @version 1.0
 * @date 2019-03-12 18:34
 */
@Component
@DisallowConcurrentExecution
public class BaseJob implements Job {
    @Resource(name = "configConstants")
    private ConfigConstants configConstants;
    @Resource
    private CommonMapper commonMapper;
    @Resource
    private JobRunDetailMapper jobRunDetailMapper;
    @Resource
    private LinkStatisticsServiceImpl linkStatisticsService;

    private JobExecutionContext newJobExecutionContext;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        newJobExecutionContext = jobExecutionContext;
        Object jobConfigurationObj = jobExecutionContext.getMergedJobDataMap().get(Constants.JOB_DATA_MAP);
        JobConfiguration jobConfiguration =
                JSON.parseObject(JSON.toJSON(jobConfigurationObj).toString(), JobConfiguration.class);
        try {
            process(jobConfiguration);
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("BaseJob的execute方法出现错误，错误原因：", e);
        }
    }

    public void process(JobConfiguration jobConfiguration) throws Exception {
        //数据发送类型
        String sendMode = jobConfiguration.getJobName();
        switch (sendMode) {
            case Constants.SEND_TYPE_HEARTBEAT:
                sendHeartBeat(jobConfiguration);
                break;
            case Constants.SEND_TYPE_ALL:
                sendMsgs(jobConfiguration, getOutputData(jobConfiguration, Constants.SEND_TYPE_ALL));
                break;
            case Constants.SEND_TYPE_INC:
                sendMsgs(jobConfiguration, getOutputData(jobConfiguration, Constants.SEND_TYPE_INC));
                break;
            default:
                break;
        }
    }

    /**
     * 获取输出数据
     *
     * @param jobConfiguration job配置
     * @param sendType         发送类型（全量/增量）
     * @return list
     */
    private List<Map<String, Object>> getOutputData(JobConfiguration jobConfiguration, String sendType) throws Exception {
        List<Map<String, Object>> outputDataList;
        if (Constants.SEND_TYPE_ALL.equals(sendType)) {
            outputDataList = commonMapper.listOutputData(jobConfiguration.getBaseTableSelectSqlAll(), null, null, null);
        } else {
            // 获取上次取数据时间
            JobRunDetailDO jobRunDetail = getLastUpdateTime(jobConfiguration);
            Date lastTime = jobRunDetail.getCheckTime() == null ? new Date() : jobRunDetail.getCheckTime();
            //发送增量数据是否需要去重
            if (jobConfiguration.getDeduplicationFlag()) {
                commonMapper.mergeData(jobConfiguration.getMergeSql(), lastTime, jobConfiguration.getLinkID());
            }
            outputDataList = commonMapper.listOutputData(jobConfiguration.getBaseTableSelectSqlInc(), lastTime, jobConfiguration.getLinkID(), null);

            // 更新本次取数据时间
            if (newJobExecutionContext != null && outputDataList != null && outputDataList.size() > 0) {
                Object updateTime = outputDataList.get(0).get(Constants.SQL_UPDATE_TIME_COLUMN_RESULT);
                Date currentTime;
                if (Constants.DATE_TYPE_DATE.equals(configConstants.getDateType())) {
                    currentTime = updateTime == null ? new Date() : ((Date) updateTime);
                } else {
                    currentTime = updateTime == null ? new Date() : DateUtil.toDate((String) updateTime, Constants.DATE_YYYYMMDDHHMMSS);
                }
                if (currentTime != null) {
                    jobRunDetail.setCheckTime(currentTime);
                    jobConfiguration.setJobRunDetail(jobRunDetail);
                    JobDataMap jobDataMap = newJobExecutionContext.getMergedJobDataMap();
                    jobDataMap.put(Constants.JOB_DATA_MAP, jobConfiguration);
                }
            }
        }

        //数据再处理
        if (outputDataList != null && outputDataList.size() > 0) {
            for (Map<String, Object> outputData : outputDataList) {
                //基础表CLOB字段的处理
                handleClob(outputData, jobConfiguration.getBasetableClobColumns());

                //获取关联表数据
                if (jobConfiguration.getHasRelationTables()) {
                    Map<Long, String> relationTableSelectSqlAllMap = jobConfiguration.getRelationTableSelectSqlAllMap();
                    Map<Long, DataBaseRelationDO> relationTableMap = jobConfiguration.getRelationTableMap();
                    Map<Long, List<String>> relationTableClobColumns = jobConfiguration.getRelationTableClobColumns();

                    for (Map.Entry<Long, String> entry : jobConfiguration.getRelationTableDataLabelMap().entrySet()) {
                        String baseTableColumn = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL
                                , relationTableMap.get(entry.getKey()).getBaseTableColumn());
                        String baseTableColumnValue = String.valueOf(outputData.get(baseTableColumn));
                        List<Map<String, Object>> relationOutputDatas = commonMapper.listOutputData(relationTableSelectSqlAllMap.get(entry.getKey()), null, null, baseTableColumnValue);
                        //关联表CLOB字段的处理
                        if (relationOutputDatas != null && relationOutputDatas.size() > 0) {
                            for (Map<String, Object> relationOutputData : relationOutputDatas) {
                                handleClob(relationOutputData, relationTableClobColumns.get(entry.getKey()));
                            }
                        }
                        //关联表的数据
                        outputData.put(entry.getValue(), relationOutputDatas);
                    }
                }
            }
        }
        return outputDataList;
    }

    /**
     * CLOB字段转换成STRING
     */
    private void handleClob(Map<String, Object> target, List<String> keys) throws Exception {
        if (keys == null || keys.size() == 0) {
            return;
        }
        for (String key : keys) {
            Clob result = (Clob) target.get(key);
            if (result != null) {
                target.put(key, result.getSubString(1, (int) result.length()));
            }
        }
    }

    /**
     * 获取上次取数据时间
     *
     * @param jobConfiguration job配置信息
     * @return cn.les.ntfm.infoshare.entity.JobRunDetailDO
     */
    private JobRunDetailDO getLastUpdateTime(JobConfiguration jobConfiguration) {
        JobRunDetailDO jobRunDetail = new JobRunDetailDO();
        jobRunDetail.setJobGroup(jobConfiguration.getJobGroup());
        jobRunDetail.setJobName(jobConfiguration.getJobName());
        JobRunDetailDO result = jobRunDetailMapper.getJobRunDetail(jobRunDetail);
        // 第一次取数据，新增获取数据的时间等信息
        if (result == null) {
            jobRunDetailMapper.addJobRunDetail(jobRunDetail);
            result = jobRunDetail;
        }
        return result;
    }

    /**
     * 发送心跳信息
     *
     * @param jobConfiguration job配置
     */
    private void sendHeartBeat(JobConfiguration jobConfiguration) throws Exception {
        ExecutorService executorService = new ThreadPoolExecutor(1
                , 1,
                0L
                , TimeUnit.MILLISECONDS
                , new LinkedBlockingQueue<>()
                , new NamedThreadFactory(Constants.SEND_POOL, jobConfiguration.getJobName()));
        for (Map.Entry<Long, String> sendTypeServiceEntry : jobConfiguration.getSendTypeServiceMap().entrySet()) {
            String heartBeatMsg = getXmlContent(jobConfiguration
                    , null
                    , null);
            executorService.execute(new SendDataThread(sendTypeServiceEntry, jobConfiguration, heartBeatMsg));
        }
        executorService.shutdown();
    }

    /**
     * 组装并发送报文到阻塞队列
     *
     * @param jobConfiguration job配置
     * @param outputDataList   输出数据
     * @throws Exception 异常
     */
    private void sendMsgs(JobConfiguration jobConfiguration, List<Map<String, Object>> outputDataList) throws Exception {
        int xmlCount = getXmlCount(outputDataList.size(), jobConfiguration.getSplitNum());
        //数据发送线程
        ExecutorService executorService = new ThreadPoolExecutor((xmlCount / 50 + 1)
                , (xmlCount / 20 + 1)
                , 0L
                , TimeUnit.MILLISECONDS
                , new LinkedBlockingQueue<>()
                , new NamedThreadFactory(Constants.SEND_POOL, jobConfiguration.getJobName()));
        //当前job输出数据量
        int outputDataListSize = outputDataList.size();
        //每条报文最大数据量
        int splitNum = jobConfiguration.getSplitNum();
        int xmlNum = getXmlCount(outputDataList.size(), jobConfiguration.getSplitNum());
        for (int i = 0; i < xmlNum; i++) {
            int itemCount = i < (xmlNum - 1) ? splitNum : (outputDataListSize - i * splitNum);
            //全量或者增量(ALL或者INC)_一次任务报文数据量_报文当前页数/报文总页数_当前报文数据量
            String statistics = jobConfiguration.getJobName()
                    + Constants.UNDERLINE + outputDataListSize
                    + Constants.UNDERLINE + (i + 1) + Constants.SLASH + xmlNum
                    + Constants.UNDERLINE + itemCount;
            List<Map<String, Object>> dataList = outputDataList.subList(i * splitNum, i * splitNum + itemCount);
            String msg = getXmlContent(jobConfiguration
                    , dataList
                    , statistics);
            for (Map.Entry<Long, String> sendTypeServiceEntry : jobConfiguration.getSendTypeServiceMap().entrySet()) {
                executorService.execute(new SendDataThread(sendTypeServiceEntry, jobConfiguration, msg));
            }
        }
        executorService.shutdown();
    }

    private int getXmlCount(int outputDataListSize, int splitNum) {

        return outputDataListSize % splitNum == 0 ? outputDataListSize / splitNum : outputDataListSize / splitNum + 1;
    }

    private String getXmlContent(JobConfiguration jobConfiguration, List<Map<String, Object>> dataList, String statistics) throws IOException, TemplateException {
        Map<String, Object> dataMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
        // 报文头部信息封装
        if (jobConfiguration.getXmlFormatConfigList() != null && jobConfiguration.getXmlFormatConfigList().size() > 0) {
            for (XmlFormatConfigDO xmlFormatConfig : jobConfiguration.getXmlFormatConfigList()) {
                if (StringUtil.isEmpty(xmlFormatConfig.getLabelType())) {
                    continue;
                }
                switch (xmlFormatConfig.getLabelType()) {
                    case Constants.XML_COLUMN_TYPE_YYYYMMDDHHMMSS:
                        if (configConstants.getTransferToUTC()) {
                            dataMap.put(xmlFormatConfig.getLabelName(), DateUtil.toString(DateUtils.getUTCTime(), Constants.DATE_YYYYMMDDHHMMSS));
                        } else {
                            dataMap.put(xmlFormatConfig.getLabelName(), DateUtil.toString(new Date(), Constants.DATE_YYYYMMDDHHMMSS));
                        }
                        break;
                    case Constants.XML_COLUMN_TYPE_SERIAL:
                        dataMap.put(xmlFormatConfig.getLabelName(), linkStatisticsService.updateSerialNumber(jobConfiguration.getLinkID()));
                        break;
                    case Constants.XML_COLUMN_TYPE_STATISTICS:
                        //全量或者增量(ALL或者INC)_一次任务报文数据量_报文当前页数/报文总页数_当前报文数据量
                        dataMap.put(xmlFormatConfig.getLabelName(), statistics);
                        break;
                    case Constants.XML_COLUMN_TYPE_SUBTYPE:
                        if (StringUtils.isEmpty(statistics)) {
                            dataMap.put(xmlFormatConfig.getLabelName(), Constants.SEND_TYPE_HEARTBEAT);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        //报文主体封装
        dataMap.put(jobConfiguration.getRootDataList(), dataList);
        //freemarker渲染
        Template template;
        if (Constants.ftlTemplateUpdateTimeMap.get(jobConfiguration.getLinkID()) != null
                && Constants.ftlTemplateUpdateTimeMap.get(jobConfiguration.getLinkID()).equals(jobConfiguration.getFtlTemplateUpdateTime())) {
            template = Constants.ftlTemplateMap.get(jobConfiguration.getLinkID());
        } else {
            FreemarkerUtil.createFtlFiles(jobConfiguration, jobConfiguration.getXmlFormatConfigList().get(0).getId());
            template = FreemarkerUtil.getTemplate(configConstants.getFtlPath(), jobConfiguration.getLinkID());
        }
        StringWriter stringWriter = new StringWriter();
        template.process(dataMap, stringWriter);
        return stringWriter.toString();
    }
}