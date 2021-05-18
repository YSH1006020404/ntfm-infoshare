package cn.les.ntfm.util;

import cn.les.framework.core.util.SpringContextUtils;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.dao.XmlFormatConfigMapper;
import cn.les.ntfm.infoshare.quartz.scheduler.ScheduleManager;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.*;


public class CommonUtil {
    @Resource
    static XmlFormatConfigMapper xmlFormatConfigMapper;
    @Autowired
    static ScheduleManager scheduleManager;

    static CommonUtil commonUtil = null;


    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat yyyyMMddFormat = new SimpleDateFormat("yyyyMMdd");
    private SimpleDateFormat yyyyMMddHHmmssFormat = new SimpleDateFormat("yyyyMMddHHmmss");


    public static CommonUtil getInstance() {
        /*if (infoshareMapper == null) {
            infoshareMapper = SpringContextUtils.getBean(InfoshareMapper.class);
        }*/
        if (xmlFormatConfigMapper == null) {
            xmlFormatConfigMapper = SpringContextUtils.getBean(XmlFormatConfigMapper.class);
        }
        if (commonUtil == null) {
            commonUtil = new CommonUtil();
        }
        if (scheduleManager == null) {
            scheduleManager = SpringContextUtils.getBean(ScheduleManager.class);
        }
        return commonUtil;
    }


    public void dataPut(String dataTypeConfig, Map map, String id) {
        List list = map.get(dataTypeConfig) == null ? new ArrayList<>() : (List) map.get(dataTypeConfig);
        list.add(id);
        map.put(dataTypeConfig, list);
    }


    /**
     * 获取主机名
     *
     * @return
     */
    public String getHostShortName() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            // hostname形如:host19.a28.com;host7.a28.com
            String hostName = addr.getHostName(); // 获取本机计算机名称
            return hostName;
        } catch (Exception e) {
            return "00";
        }
    }


    /**
     * 打开scheduler
     */
    public Boolean openSch() {
        Boolean flag = true;
        try {
            if (scheduleManager.getScheduler().isInStandbyMode()) {
                scheduleManager.getScheduler().start();
                //  System.out.println("dakaihou####################"+!scheduleManager.getScheduler().isInStandbyMode());
                Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.SCHSTATUS + LogConstants.WARN_MSG).warn("打开调度器");
                flag = true;
            } else {
                //  System.out.println("已经为打开状态####################"+!scheduleManager.getScheduler().isInStandbyMode());
                Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.SCHSTATUS + LogConstants.WARN_MSG).warn("调度器已经为 打开 状态");

                flag = true;

            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }


    /**
     * 关闭scheduler
     */
    public Boolean closeSch() {
        Boolean flag = true;
        try {
            if (!scheduleManager.getScheduler().isInStandbyMode()) {

                scheduleManager.getScheduler().standby();
                // System.out.println("gunabihou%%%%%%%%%%%%%%%%%%"+!scheduleManager.getScheduler().isInStandbyMode());
                Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.SCHSTATUS + LogConstants.WARN_MSG).warn("关闭调度器");
                flag = true;
            } else {
                // System.out.println("已经为关闭状态%%%%%%%%%%%%%%%%%%"+!scheduleManager.getScheduler().isInStandbyMode());
                Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.SCHSTATUS + LogConstants.WARN_MSG).warn("调度器已经为 关闭 状态");

                flag = true;
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }


}
