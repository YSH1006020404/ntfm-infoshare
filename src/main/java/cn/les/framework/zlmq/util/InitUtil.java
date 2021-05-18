package cn.les.framework.zlmq.util;

import cn.les.framework.zlmq.constant.ZoneCodeEnum;
import jni.ZLCallUtil;

public class InitUtil {
    private static String REGION = "";

    public InitUtil() {
    }

    public static String getREGION() {
        return REGION;
    }

    public static void initRegion(String[] args) {
        int regionID = 0;
        if (args.length >= 1) {
            regionID = ZLCallUtil.getZoneID();
        }

        switch (regionID) {
            case 1:
                REGION = ZoneCodeEnum.CENTER.getZoneCode();
                break;
            case 2:
                REGION = ZoneCodeEnum.STANDBY.getZoneCode();
                break;
            case 3:
                REGION = ZoneCodeEnum.EMERGENCY.getZoneCode();
                break;
            case 4:
                REGION = ZoneCodeEnum.DMZ.getZoneCode();
                break;
            case 5:
                REGION = ZoneCodeEnum.ITOM.getZoneCode();
                break;
            case 6:
                REGION = ZoneCodeEnum.MONITOR.getZoneCode();
                break;
            case 7:
            case 8:
            case 9:
            case 19:
            case 20:
            default:
                REGION = ZoneCodeEnum.DEFAULT.getZoneCode();
                break;
            case 10:
                REGION = ZoneCodeEnum.OMC.getZoneCode();
                break;
            case 11:
                REGION = ZoneCodeEnum.ZB_REGION.getZoneCode();
                break;
            case 12:
                REGION = ZoneCodeEnum.ZY_REGION.getZoneCode();
                break;
            case 13:
                REGION = ZoneCodeEnum.ZS_REGION.getZoneCode();
                break;
            case 14:
                REGION = ZoneCodeEnum.ZG_REGION.getZoneCode();
                break;
            case 15:
                REGION = ZoneCodeEnum.ZJ_REGION.getZoneCode();
                break;
            case 16:
                REGION = ZoneCodeEnum.ZU_REGION.getZoneCode();
                break;
            case 17:
                REGION = ZoneCodeEnum.ZL_REGION.getZoneCode();
                break;
            case 18:
                REGION = ZoneCodeEnum.ZW_REGION.getZoneCode();
                break;
            case 21:
                REGION = ZoneCodeEnum.NFPS.getZoneCode();
                break;
            case 32:
                REGION = ZoneCodeEnum.EMERGENCY_DMZ.getZoneCode();
        }

    }
}
