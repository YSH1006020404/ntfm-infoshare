package cn.les.ntfm.infoshare.service.impl;

import cn.les.framework.core.util.DateUtil;
import cn.les.framework.web.base.impl.BaseServiceImpl;
import cn.les.ntfm.constant.ConfigConstants;
import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.infoshare.entity.LinkStatisticsDO;
import cn.les.ntfm.infoshare.dao.LinkStatisticsMapper;
import cn.les.ntfm.infoshare.service.LinkStatisticsService;
import cn.les.ntfm.util.DateUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 链路统计service
 *
 * @author 杨硕
 * @create 2020-07-08 9:28
 */
@Service("linkStatisticsService")
public class LinkStatisticsServiceImpl extends BaseServiceImpl implements LinkStatisticsService {
    @Resource(name = "configConstants")
    private ConfigConstants configConstants;
    @Resource
    private LinkStatisticsMapper linkStatisticsMapper;

    @Override
    public String updateSerialNumber(Long infoshareconfigId) {
        String currentTime = getCurrentTime();
        //当天的序列数据是否存在
        LinkStatisticsDO param = new LinkStatisticsDO();
        param.setInfoshareconfigId(infoshareconfigId);
        param.setStatisticsTime(currentTime);
        List<LinkStatisticsDO> serialNumberList = linkStatisticsMapper.listData(param);
        LinkStatisticsDO linkStatisticsDO;
        if (serialNumberList == null || serialNumberList.size() == 0) {
            //当天的序列统计不存在
            param.setSerialNumber(1L);
            linkStatisticsMapper.addData(param);
            linkStatisticsDO = linkStatisticsMapper.getDataById(param.getId());
        } else {
            //当天的序列统计存在
            linkStatisticsMapper.updateSerialNumber(param);
            linkStatisticsDO = linkStatisticsMapper.getDataById(serialNumberList.get(0).getId());
        }
        String result = currentTime + Constants.UNDERLINE + infoshareconfigId + Constants.UNDERLINE
                + StringUtils.leftPad(String.valueOf(linkStatisticsDO.getSerialNumber()), 10, '0');
        return result;
    }

    @Override
    public void updateStatisticsCount(Long infoshareconfigId, Long destinationconfigId) {
        String currentTime = getCurrentTime();
        //当天的统计数据是否存在
        LinkStatisticsDO linkStatisticsDO = new LinkStatisticsDO();
        linkStatisticsDO.setInfoshareconfigId(infoshareconfigId);
        linkStatisticsDO.setDestinationconfigId(destinationconfigId);
        linkStatisticsDO.setStatisticsTime(currentTime);
        List<LinkStatisticsDO> statisticsCountList = linkStatisticsMapper.listData(linkStatisticsDO);
        if (statisticsCountList == null || statisticsCountList.size() == 0) {
            //当天的序列统计不存在
            linkStatisticsDO.setStatisticsCount(1L);
            linkStatisticsMapper.addData(linkStatisticsDO);
        } else {
            //当天的序列统计存在
            linkStatisticsMapper.updateStatisticsCount(linkStatisticsDO);
        }
    }

    /**
     * 获取当前时间
     *
     * @return String
     */
    public String getCurrentTime() {
        //当前时间
        String currentTime;
        if (configConstants.getTransferToUTC()) {
            currentTime = DateUtil.toString(DateUtils.getUTCTime(), Constants.DATE_YYYYMMDD);
        } else {
            currentTime = DateUtil.toString(new Date(), Constants.DATE_YYYYMMDD);
        }
        return currentTime;
    }
}
