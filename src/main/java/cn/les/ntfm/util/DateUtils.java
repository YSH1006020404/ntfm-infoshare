package cn.les.ntfm.util;

import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类
 *
 * @author 杨硕
 * @date 2020-07-06 19:18
 */
public class DateUtils {
    /**
     * 获取当前时间（UTC时）
     *
     * @return date
     */
    public static Date getUTCTime() {
        Calendar calendar = Calendar.getInstance();
        //时间偏移量
        int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
        //夏令时差
        int dstOffset = calendar.get(Calendar.DST_OFFSET);
        calendar.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        return calendar.getTime();
    }
}
