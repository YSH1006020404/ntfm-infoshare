package cn.les.ntfm.infoshare.service.impl;

import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.util.Log4jUtils;
import org.apache.activemq.AsyncCallback;

import javax.jms.JMSException;

/**
 * activemq发送结果
 *
 * @author 杨硕
 * @create 2019-10-16 15:50
 */
public class MyAsyncCallback implements AsyncCallback {
    private String logPath;
    private String msg;

    MyAsyncCallback(String logPath, String msg) {
        this.logPath = logPath;
        this.msg = msg;
    }

    @Override
    public void onSuccess() {
//        Log4jUtils.getInstance().getLogger(logPath + LogConstants.INFO_MSG)
//                .info("msg send to mq " + destination + " success");

    }

    @Override
    public void onException(JMSException e) {
        Log4jUtils.getInstance().getLogger(logPath + LogConstants.ERROR_MSG)
                .error(msg + " failed to send to activemq:" + e);
    }
}
