package cn.les.ntfm.infoshare.service.impl;

import cn.les.framework.core.util.DateUtil;
import cn.les.framework.core.util.SpringContextUtils;
import cn.les.ntfm.constant.ConfigConstants;
import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.entity.FtpConfigDO;
import cn.les.ntfm.util.DateUtils;
import cn.les.ntfm.util.FtpUtil;
import cn.les.ntfm.util.Log4jUtils;
import cn.les.ntfm.util.NamedThreadFactory;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FtpMsgSender {
    private ConfigConstants configConstants;
    private FTPClient ftpClient;
    private ScheduledThreadPoolExecutor executorService;
    private FtpConfigDO ftpConfigDO;

    public FtpMsgSender(FtpConfigDO ftpConfigDO, Long threadGroupName) {
        this.configConstants = SpringContextUtils.getBean(ConfigConstants.class);
        this.ftpConfigDO = ftpConfigDO;
        executorService = new ScheduledThreadPoolExecutor(
                5, new NamedThreadFactory(Constants.SEND_POOL, String.valueOf(threadGroupName)));
        // 创建连接
        ftpClient = FtpUtil.createFtpClient(ftpConfigDO);
        // 增加重连功能
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (FtpUtil.isConnectClosed(ftpClient)) {
                    ftpClient = FtpUtil.createFtpClient(ftpConfigDO);
                }
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void sendMsg(String msg, String logPath) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 在tmp目录下生成一个时间戳的xml文件
                    String date = (configConstants.getTransferToUTC()
                            ? DateUtil.toString(DateUtils.getUTCTime(), Constants.DATE_YYYYMMDDHHMMSSSSS)
                            : DateUtil.toString(new Date(), Constants.DATE_YYYYMMDDHHMMSSSSS));
                    String filename = ftpConfigDO.getObjectName() + "_" + date + ".xml";
                    String localpath = "/tmp/";
                    File file = new File(localpath + filename);
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            Log4jUtils.getInstance().getLogger(logPath + LogConstants.ERROR_MSG)
                                    .error("FtpMsgSender类的sendMsg方法（创建文件）出现错误，错误原因：" + e);
                        }
                    }
                    byte[] bytes = msg.getBytes();
                    //  int b = bytes.length;   //是字节的长度，不是字符串的长度
                    FileInputStream in = null;
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(bytes);
                    fos.flush();
                    fos.close();
                    in = new FileInputStream(file);


                    if (FtpUtil.uploadToFtp(ftpClient, ftpConfigDO, in, filename)) {
                        file.delete();
                        Log4jUtils.getInstance().getLogger(logPath + LogConstants.SEND_MSG)
                                .warn("send msg:" + msg);
                    } else {
                        Log4jUtils.getInstance().getLogger(logPath + LogConstants.ERROR_MSG)
                                .error("上传文件方法出错！");
                    }
                } catch (Exception e) {
                    Log4jUtils.getInstance().getLogger(logPath + LogConstants.ERROR_MSG)
                            .error("FtpMsgSender类的sendMsg方法出现错误，错误原因：", e);
                }
            }
        });
    }


    public ScheduledThreadPoolExecutor getExecutorService() {
        return executorService;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }
}
