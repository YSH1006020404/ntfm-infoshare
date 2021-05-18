package cn.les.ntfm.infoshare.runner;

import cn.les.ntfm.constant.ConfigConstants;
import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.entity.CheckConnDo;
import cn.les.ntfm.infoshare.dao.CheckConnMapper;
import cn.les.ntfm.infoshare.quartz.scheduler.ScheduleManager;
import cn.les.ntfm.util.Log4jUtils;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by jxh on2019/10/10
 */
@Component
public class CheckConn {
    @Autowired
    private ScheduleManager scheduleManager;
    @Resource
    CheckConnMapper checkConnMapper;
    @Autowired
    private ConfigConstants configConstants;


    /**
     * 服务器当前状态 初始化正常
     */
    Boolean statusDB = true;
    Boolean statusIbm = true;
    Boolean statusActive = true;
    Boolean statusFtp = true;


    /**
     * 计数连续状态false的次数
     */
    int databaseNum = 0;
    int activeNum = 0;
    int ibmNum = 0;
    int ftpNum = 0;

    /**
     * 本机主机名
     */
    String host;

    /**
     * 3个服务器检测的最大时间
     */
    int maxperiod;

    /**
     * 数据库连接失败恢复后，conn计数失败，初始化重新开开始
     */
    private void init() {
        statusDB = true;
        statusIbm = true;
        statusActive = true;
        statusFtp = true;

        databaseNum = 0;
        activeNum = 0;
        ibmNum = 0;
        ftpNum = 0;
        checkConnMapper.initInstance(host);
    }

    /**
     * 探寻dataBase  ibmmq active ftp的连接状态并控制scheduler
     */
   // @PostConstruct
    void checkConnect() {
        maxperiod = Math.max(Math.max(configConstants.getCheckFtpPeriod(), configConstants.getCheckActivePeriod()), configConstants.getCheckIbmPeriod());
        host = getHostShortName();
        checkConnMapper.initInstance(host);
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(6);
        /**
         *更新checkTime(标志实例存活)
         */
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // 更新checkTime
                checkConnMapper.updateCheckTime(host);
                Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.CHECKTIME + LogConstants.INFO_MSG).info("I am " + host);

            }
        }, 0, maxperiod, TimeUnit.SECONDS);

        /**
         * 检查conn（用于控制scheduler）
         */
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (Constants.PAUSE.equals(checkConnMapper.queryInstance(host).getOper())) {
                    closeSch();
                } else {
                    if (ifShutdownSch(host)) {
                        closeSch();
                    } else {
                        openSch();
                    }
                }


                try {
                    Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.SCHSTATUS + LogConstants.INFO_MSG).info("scheduler current status：" + !scheduleManager.getScheduler().isInStandbyMode());

                    //  System.out.println("调度器当前的状态："+!scheduleManager.getScheduler().isInStandbyMode());
                } catch (SchedulerException e) {

                    Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.ERROR_MSG).error("CheckConn类的checkConnect方法(write log error)出现错误，错误原因："+e);

                }

            }

        }, 0, 2 * maxperiod, TimeUnit.SECONDS);
        /**
         *
         * 定期检查数据库的状态)
         */
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (configConstants.getIsOracleTest()) {
                    // 默认正常
                    try {
                        checkDatasource(configConstants.getOracleDriver(), configConstants.getOracleURL(), configConstants.getOracleUserName(), configConstants.getOraclePwd(), host);
                    } catch (Exception e) {
                        Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.DATABASECHECK + LogConstants.ERROR_MSG).error("CheckConn类的checkConnect方法(dataBase error)出现错误，错误原因:" + e);

                    }

                }

            }
        }, 0, configConstants.getCheckDBPeriod(), TimeUnit.SECONDS);


        /**
         * 定期检查active状态
         */
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                if (configConstants.getIsActiveTest()) {
                    checkActive(configConstants.getActiveURL(), host);
                }

            }
        }, 0, configConstants.getCheckActivePeriod(), TimeUnit.SECONDS);

        /**
         * 定期检查ibm的状态
         */
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                if (configConstants.getIsIbmTest()) {
                    try {

                        checkIbm(configConstants.getIbmURL(), configConstants.getIbmManager(), configConstants.getChannel(), host);
                    } catch (Exception e) {
                      //  System.out.println("ibm error: " + e);
                        Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.IBMCHECK + LogConstants.ERROR_MSG).error("CheckConn类的checkConnect方法(ibm error)出现错误，错误原因:" + e);

                    }

                    //  System.out.println("ibm状态：" + checkIbm(ibmURL, ibmManager, channel, host));
                }

            }
        }, 0, configConstants.getCheckIbmPeriod(), TimeUnit.SECONDS);

        /**
         * 定期检查ftp的状态
         */
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                if (configConstants.getIsFtpTest()) {

                    checkFtp(configConstants.getFtpURL(), configConstants.getFtpUserName(), configConstants.getFtpPwd(), configConstants.getFtpPort(), host);
                }
            }
        }, 0, configConstants.getCheckFtpPeriod(), TimeUnit.SECONDS);


    }


    /**
     * 测试数据库的连接状态
     *
     * @param driveClass
     * @param url
     * @param username
     * @param password
     * @return
     */
    private Boolean checkDatasource(String driveClass, String url, String username, String password, String host) {
        java.sql.Connection conn = null;
        try {
            Class.forName(driveClass);
            conn = DriverManager.getConnection(url, username, password);
            // 坏->好
            if (!statusDB) {
                init();
                // 若调度器没有被人工关闭
                if (!Constants.PAUSE.equals(checkConnMapper.queryInstance(host).getOper())) {
                    openSch();
                }
            }
            statusDB = true;
            databaseNum = 0;
        } catch (Exception e) {
            databaseNum++;
            if (databaseNum == configConstants.getComfirmPeriod()) {
                // 关闭scheduler（开则关闭，关闭则不作操作）
                closeSch();
                statusDB = false;
            } else if (databaseNum > configConstants.getComfirmPeriod()) {
                databaseNum = 0;
            }
            Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.DATABASECHECK + LogConstants.ERROR_MSG).error("CheckConn类的checkDatasource方法(database connect error)出现错误，错误原因:" + e);


        } finally {
            // 关闭连接
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.DATABASECHECK + LogConstants.ERROR_MSG).error("CheckConn类的checkDatasource方法(database close error)出现错误，错误原因:" + e);

                }
            }
            Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.DATABASECHECK + LogConstants.WARN_MSG).warn("--数据库状态" + statusDB);
        }
        return statusDB;

    }

    /**
     * 测试active的连接状态
     *
     * @param brokeURL
     * @return
     */
    private Boolean checkActive(String brokeURL, String host) {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokeURL);
        try {
            ActiveMQConnection connection = (ActiveMQConnection) connectionFactory.createConnection(configConstants.getActiveUserName(), configConstants.getActivePwd());
            ExecutorService excutorService = Executors.newSingleThreadExecutor();
            excutorService.execute(() -> {
                try {
                    connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                } catch (JMSException e) {
//                    Map<String, Object> map = falseOper(activeNum, statusActive);
//                    activeNum = (int) map.get("num");
//                    statusActive = (Boolean) map.get("status");
                   /* System.out.println("--active连接状态：" + statusActive);
                    System.out.println("--active创建连接会话报错：" + e);*/
                    Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.ACTIVECHECK + LogConstants.ERROR_MSG).error("CheckConn类的checkActive方法(active create seesion)出现错误，错误原因:" + e);


                }
            });

            // 睡眠 等待以获取连接状态
            try {
                Thread.sleep(configConstants.getActiveSleepTime() * 1000);
            } catch (InterruptedException e) {
            }

            if (connection.getTransport().isConnected()) {
                // 根据状态执行相关操作
                statusChangedOper(statusActive, true, host);
                statusActive = true;
                activeNum = 0;
                // System.out.println("--active连接状态：" + statusActive);
            } else {
                Map<String, Object> map = falseOper(activeNum, statusActive);
                activeNum = (int) map.get("num");
                statusActive = (Boolean) map.get("status");
                //  System.out.println("--active连接状态：" + statusActive);
                //  Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.ACTIVECHECK + LogConstants.WARN_MSG).warn(new Date() + "--active连接状态：" + statusActive);

            }
            try {
                // 关闭连接
             //   connection.getTransport().stop();
                connection.close();
            } catch (Exception e) {
                // System.out.println("--active：connection.getTransport().stop()报错：" + e);
                Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.ACTIVECHECK + LogConstants.ERROR_MSG).error("CheckConn类的checkActive方法(active connection.close())出现错误，错误原因:" + e);
            }

        } catch (JMSException e) {
            // System.out.println("--active测试连接报错：" + e);
            Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.ACTIVECHECK + LogConstants.ERROR_MSG).error("CheckConn类的checkActive方法出现错误，错误原因:" + e);
        }

        Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.ACTIVECHECK + LogConstants.INFO_MSG).info("--active连接状态：" + statusActive);

        return statusActive;

    }

    /**
     * 测试ibm的连接状态
     *
     * @param URL
     * @param manager
     * @return
     */
    private Boolean checkIbm(String URL, String manager, String channel, String host) {
        try {
            MQQueueConnectionFactory connectionFactory = new MQQueueConnectionFactory();
            connectionFactory.setConnectionNameList(URL);
            connectionFactory.setQueueManager(manager);
            connectionFactory.setChannel(channel);
            connectionFactory.setTransportType(1);
            Connection conn = connectionFactory.createConnection();

            statusChangedOper(statusIbm, true, host);
            statusIbm = true;
            ibmNum = 0;
            //  Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.IBMCHECK + LogConstants.WARN_MSG).warn(new Date() + "--ibm连接状态：" + statusIbm);

        } catch (JMSException e) {

            Map<String, Object> map = falseOper(ibmNum, statusIbm);
            ibmNum = (int) map.get("num");
            statusIbm = (Boolean) map.get("status");

            // Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.IBMCHECK + LogConstants.WARN_MSG).warn(new Date() + "--ibm状态" + statusIbm);
            Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.IBMCHECK + LogConstants.ERROR_MSG).error("CheckConn类的checkIbm方法(ibm create seesion)出现错误，错误原因:" + e);

        }
        Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.IBMCHECK + LogConstants.WARN_MSG).warn("--ibm状态" + statusIbm);

        return statusIbm;

    }

    /**
     * 测试ftp的连接状态
     *
     * @param ftpHost
     * @param ftpUserName
     * @param ftpPassword
     * @param ftpPort
     * @return
     */
    private Boolean checkFtp(String ftpHost, String ftpUserName, String ftpPassword, int ftpPort, String host) {
        FTPClient ftpClient = new FTPClient();
        try {
            // 连接FTP服务
            ftpClient.connect(ftpHost, ftpPort);
            // 登陆FTP服务
            ftpClient.login(ftpUserName, ftpPassword);
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                //  System.out.println("未连接到FTP，用户名或密码错误");
                ftpClient.disconnect();

                Map<String, Object> map = falseOper(ftpNum, statusFtp);
                ftpNum = (int) map.get("num");
                statusFtp = (Boolean) map.get("status");

                Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.FTPCHECK + LogConstants.ERROR_MSG).error("连接到FTP，用户名或密码错误");

            } else {
                //  System.out.println("FTP连接成功");

                statusChangedOper(statusFtp, true, host);
                statusFtp = true;
                ftpNum = 0;

            }
        } catch (SocketException e) {
         //   e.printStackTrace();
            //    System.out.println("FTP的IP地址可能错误，请正确配置");

            Map<String, Object> map = falseOper(ftpNum, statusFtp);
            ftpNum = (int) map.get("num");
            statusFtp = (Boolean) map.get("status");
            Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.FTPCHECK + LogConstants.ERROR_MSG).error("连接到FTP，用户名或密码错误");


        } catch (IOException e) {
           // e.printStackTrace();
            //  System.out.println("FTP的端口错误,请正确配置");

            Map<String, Object> map = falseOper(ftpNum, statusFtp);
            ftpNum = (int) map.get("num");
            statusFtp = (Boolean) map.get("status");
            Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.FTPCHECK + LogConstants.ERROR_MSG).error("连接到FTP，用户名或密码错误");

        }
        Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.FTPCHECK + LogConstants.INFO_MSG).info("--ftp状态" + statusFtp);

        return statusFtp;
    }

    /**
     * 服务器连接失败操作，连接失败有个确认周期，确认周期在配置文件中配
     *
     * @param num
     * @param status
     */
    private Map<String, Object> falseOper(int num, Boolean status) {
        Map<String, Object> map = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
        num++;
        if (num == configConstants.getComfirmPeriod()) {
            statusChangedOper(status, false, host);
            status = false;
        } else if (num > configConstants.getComfirmPeriod()) {
            num = 0;
        }
        map.put("status", status);
        map.put("num", num);
        return map;
    }

    /**
     * 比较不同实例的状态  返回true则关闭scheduler
     *
     * @param host
     * @return
     */
    private Boolean ifShutdownSch(String host) {
        List<CheckConnDo> checkConnDos = checkConnMapper.queryInstances();
        //初始化
        Boolean flag = false;

        //只有一个实例,不关闭
        //多个实例
        if (checkConnDos.size() > 1) {
            //找到本机
            CheckConnDo checkConnDo = getCurrentHost(checkConnDos);
            //本机实例conn大，且对方存活，则关闭
            Map<String, Boolean> map = instanceStatus(checkConnDos);
            for (CheckConnDo e : checkConnDos) {
                if (checkConnDo.compareTo(e) > 0 && map.get(e.getHost())) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;

    }


    /**
     * 获取每个实例的存活状态(oper为pause)
     *
     * @param list
     * @return
     */
    private Map<String, Boolean> instanceStatus(List<CheckConnDo> list) {
        Map<String, Boolean> instanceStatusMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
        for (CheckConnDo e : list) {
            if ((System.currentTimeMillis() - e.getCheckTime().getTime()) / 1000 < configConstants.getMissCheckPeriod() * maxperiod&&  !Constants.PAUSE.equals(checkConnMapper.queryInstance(e.getHost()).getOper())) {
                instanceStatusMap.put(e.getHost(), true);
            } else {
                instanceStatusMap.put(e.getHost(), false);

            }
        }
        return instanceStatusMap;

    }

    /**
     * 状态发生改变时，更新checkConn数据表,并控制任务调度器
     *
     * @param status      上次状态
     * @param isCurrtenOk 当前状态
     * @param host
     */
    private void statusChangedOper(Boolean status, Boolean isCurrtenOk, String host) {
        if (!status && isCurrtenOk) {//坏->好
            try {
                checkConnMapper.updateCheckConn(host, -1);
            } catch (Exception e) {
                Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.INFOSHARE + LogConstants.ERROR_MSG).error("CheckConn类的statusChangedOper方法(updateCheckConn error)出现错误，错误原因:" + e);

            }
        } else if (status && !isCurrtenOk) {  //好->坏

            checkConnMapper.updateCheckConn(host, 1);
        }
    }


    /**
     * 从所有实例中找到当前实例
     *
     * @param checkConnDos
     * @return
     */
    private CheckConnDo getCurrentHost(List<CheckConnDo> checkConnDos) {
        CheckConnDo checkConnDo = null;
        for (CheckConnDo e : checkConnDos) {
            if (e.getHost().equals(host)) {
                checkConnDo = e;
                break;
            }
        }
        return checkConnDo;
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
            Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.INFOSHARE + LogConstants.ERROR_MSG).error("CheckConn类的getHostShortName方法出现错误，错误原因:" + e);

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
                Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.SCHSTATUS + LogConstants.INFO_MSG).info("打开调度器");
                flag = true;
            } else {
                //  System.out.println("已经为打开状态####################"+!scheduleManager.getScheduler().isInStandbyMode());
                Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.SCHSTATUS + LogConstants.INFO_MSG).info("调度器已经为 打开 状态");

                flag = true;

            }
        } catch (SchedulerException e) {
           // e.printStackTrace();
            flag = false;
            Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.INFOSHARE + LogConstants.ERROR_MSG).error("CheckConn类的openSch方法出现错误，错误原因:" + e);

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
                Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.SCHSTATUS + LogConstants.INFO_MSG).info("关闭调度器");
                flag = true;
            } else {
                // System.out.println("已经为关闭状态%%%%%%%%%%%%%%%%%%"+!scheduleManager.getScheduler().isInStandbyMode());
                Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.SCHSTATUS + LogConstants.INFO_MSG).info("调度器已经为 关闭 状态");

                flag = true;
            }
        } catch (SchedulerException e) {
           // e.printStackTrace();
            flag = false;
            Log4jUtils.getInstance().getLogger(LogConstants.CHECKCONN + LogConstants.INFOSHARE + LogConstants.ERROR_MSG).error("CheckConn类的closeSch方法出现错误，错误原因:" + e);

        }
        return flag;
    }

}
