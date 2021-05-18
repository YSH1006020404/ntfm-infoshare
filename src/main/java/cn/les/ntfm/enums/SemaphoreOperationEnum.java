package cn.les.ntfm.enums;

/**
 * 信号量操作类型
 *
 * @author 杨硕
 * @create 2019-12-30 13:23
 */
public enum SemaphoreOperationEnum {
    /**
     * 获取许可
     */
    ACQUIRE("acquire"),
    /**
     * 释放许可
     */
    RELEASE("release");
    private final String value;

    SemaphoreOperationEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
