package cn.les.framework.zlmq.constant;

public enum ZoneCodeEnum {
    DEFAULT(0, "default"),
    CENTER(1, "center"),
    STANDBY(2, "standby"),
    EMERGENCY(3, "emergency"),
    DMZ(4, "dmz"),
    ITOM(5, "itom"),
    MONITOR(6, "monitor"),
    OMC(10, "omc"),
    ZB_REGION(11, "zb_region"),
    ZY_REGION(12, "zy_region"),
    ZS_REGION(13, "zs_region"),
    ZG_REGION(14, "zg_region"),
    ZJ_REGION(15, "zj_region"),
    ZU_REGION(16, "zu_region"),
    ZL_REGION(17, "zl_region"),
    ZW_REGION(18, "zw_region"),
    NFPS(21, "nfps"),
    EMERGENCY_DMZ(32, "emerg_dmz");

    private int zoneID;
    private String zoneCode;

    private ZoneCodeEnum(int zoneID, String zoneCode) {
        this.zoneID = zoneID;
        this.zoneCode = zoneCode;
    }

    public int getZoneID() {
        return this.zoneID;
    }

    public String getZoneCode() {
        return this.zoneCode;
    }

}
