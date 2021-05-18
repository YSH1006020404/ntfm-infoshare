package cn.les.ntfm.infoshareConf.service;

import cn.les.framework.core.pagination.PageResult;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

public interface InfoshareService {
    PageResult<Map<String, Object>> listMsgs(Map<String, Object> params);

    void saveEditMsgs(JSONObject data) throws Exception;

    void updateEditMsgs(JSONObject data) throws Exception;

    Map<String, Object> getMsgsById(long id);

    void updateCopyLinks(Long[] infoshareIds) throws Exception;

    void removeLink(Long[] infoshareIds) throws Exception;

}
