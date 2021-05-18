package cn.les.ntfm.infoshare.service;

public interface SerialNumberService {

    /**
     * 生成流水号
     *
     * @param linkid 链路主键
     * @return
     */
    String outputSerialNumber(Long linkid);
}
