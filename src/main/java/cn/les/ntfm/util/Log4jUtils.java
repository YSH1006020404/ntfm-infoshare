package cn.les.ntfm.util;

import cn.les.framework.core.util.SpringContextUtils;
import cn.les.ntfm.constant.ConfigConstants;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志工具类
 *
 * @author 杨硕
 * @create 2019-05-24 15:31
 */
public class Log4jUtils {
    private final static ConcurrentHashMap<String, Logger> loggerMap = new ConcurrentHashMap<>();
    private static String shortName;
    private static Log4jUtils signal = null;
    @Resource(name = "configConstants")
    protected ConfigConstants configConstants;

    public Log4jUtils() {
        if (configConstants == null) {
            configConstants = SpringContextUtils.getBean(ConfigConstants.class);
        }
    }

    public static Log4jUtils getInstance() {
        if (signal == null) {
            signal = new Log4jUtils();

        }
        return signal;
    }


    static {
        try {
            // hostname形如:host19.a28.com;host7.a28.com
            String hostName = InetAddress.getLocalHost().getHostName().toString();
            shortName = hostName.split("\\.")[0];
            shortName = shortName.substring(4, shortName.length()).concat("a");
        } catch (UnknownHostException e) {
            shortName = "00";
        }
    }

    public synchronized Logger getLogger(String name) {
        Logger logger = loggerMap.get(name);
        if (null != logger) {
            return logger;
        }
        return createNewLogger(name);
    }

    private Logger createNewLogger(String name) {
        Logger logger = Logger.getLogger(name);
        logger.removeAllAppenders();
        logger.setLevel(Level.WARN);
        logger.setAdditivity(true);
        DailyRollingFileAppender appender = new DailyRollingFileAppender();
        appender.setName(name);
        PatternLayout layout = new PatternLayout();
        String conversionPatten = "%d - %5p %t %C.%M(%L) - %m%n";
        layout.setConversionPattern(conversionPatten);
        appender.setLayout(layout);
        appender.setFile(configConstants.getLogBasePath()
                .concat(name).concat("_").concat(shortName));
        appender.setEncoding("utf-8");
        appender.setDatePattern("'_'yyyy-MM-dd-HH'.log'");
        appender.setAppend(true);
        appender.activateOptions();
        logger.addAppender(appender);
        loggerMap.put(name, logger);
        return logger;
    }
}
