package cn.les.ntfm.infoshare.service.impl;

import cn.les.framework.core.util.DateUtil;
import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.infoshare.dao.SerialNumberMapper;
import cn.les.ntfm.infoshare.service.SerialNumberService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service(value = "serialNumberService")
public class SerialNumberServiceImpl implements SerialNumberService {
    @Resource
    private SerialNumberMapper serialNumberMapper;

    @Override
    public String outputSerialNumber(Long linkid) {
        Map<String, Object> queryData = serialNumberMapper.queryData(linkid);
        String sendCounter = "00000000";

        // 如果该条链路的流水号信息为空，给其在表中赋初值
        if (queryData == null || queryData.size() == 0) {
            queryData = new HashMap<>();
            sendCounter = getSerialNumber(sendCounter);
            String sendTime = DateUtil.toString(new Date(), Constants.DATE_YYYYMMDD);
            queryData.put("sendCounter", sendCounter);
            queryData.put("sendTime", sendTime);
            queryData.put("linkid", linkid);
            serialNumberMapper.insertData(queryData);
            return sendTime + "_" + linkid + "_" + sendCounter;
        } else {
            if (queryData.get("sendTime").equals(DateUtil.toString(new Date(), Constants.DATE_YYYYMMDD))) {
                sendCounter = getSerialNumber(queryData.get("sendCounter").toString());
                String sendTime = (String) queryData.get("sendTime");
                queryData.put("sendCounter", sendCounter);
                serialNumberMapper.updateData(queryData);
                return sendTime + "_" + linkid + "_" + sendCounter;
            } else {
                sendCounter = getSerialNumber(sendCounter);
                String sendTime = DateUtil.toString(new Date(), Constants.DATE_YYYYMMDD);
                queryData.put("sendTime", sendTime);
                queryData.put("sendCounter", sendCounter);
                serialNumberMapper.updateData(queryData);
                return sendTime + "_" + linkid + "_" + sendCounter;
            }
        }
    }

    private String getSerialNumber(String sendCounter) {
        long parseLong = Long.parseLong(sendCounter) + 1;
        String newSendCounter = String.format("%08d", parseLong);
        return newSendCounter;
    }
}
