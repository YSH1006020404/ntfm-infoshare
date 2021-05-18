package cn.les.ntfm.infoshareConf.controller;

import cn.les.framework.core.result.R;
import cn.les.framework.web.base.impl.BaseControllerImpl;
import cn.les.ntfm.constant.ConfigConstants;
import cn.les.ntfm.constant.PropertyNameConstants;
import cn.les.ntfm.infoshare.entity.InfoshareConfigDO;
import cn.les.ntfm.infoshare.dao.CheckConnMapper;
import cn.les.ntfm.infoshare.service.JobConfigurationService;
import cn.les.ntfm.infoshareConf.service.InfoshareService;
import cn.les.ntfm.util.CommonUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Map;

@Controller
@RequestMapping("/infoshareConf/infoshare")
public class InfoshareController extends BaseControllerImpl {
    @Resource
    private InfoshareService infoshareService;
    @Resource
    private CheckConnMapper checkConnMapper;
    @Resource
    private JobConfigurationService jobConfigurationService;
    @Resource
    private ConfigConstants configConstants;

    /**
     * 首页链路查询
     */
    @RequestMapping("/queryMsgs")
    @ResponseBody
    public R queryMsgs(@RequestParam Map<String, Object> params) {
        return R.ok(infoshareService.listMsgs(params));
    }

    /**
     * 新增链路 保存
     */
    @RequestMapping("/insertMsgs")
    @ResponseBody
    public synchronized R insertMsgs(@RequestBody JSONObject data) throws Exception {
        //数据库新增
        infoshareService.saveEditMsgs(data);
        return R.ok(true);
    }

    /**
     * 编辑链路 保存
     */
    @RequestMapping("/editMsgs")
    @ResponseBody
    public synchronized R editMsgs(@RequestBody JSONObject data) throws Exception {
        //数据库修改
        infoshareService.updateEditMsgs(data);
        InfoshareConfigDO infoshareConfigDO = JSON.parseObject(JSON.toJSONString(data.get("infoshareConfig")), InfoshareConfigDO.class);
        //修改job
        jobConfigurationService.delJob(infoshareConfigDO.getId());
        jobConfigurationService.addJob(infoshareConfigDO.getId());
        return R.ok(true);
    }

    /**
     * 通过id 查询编辑界面需要显示的信息
     */
    @RequestMapping("/queryMsgsById")
    @ResponseBody
    public R queryMsgsById(@RequestParam("id") Long id) {
        return R.ok(infoshareService.getMsgsById(id));
    }

    /**
     * 删除链路
     */
    @RequestMapping("/delLinks")
    @ResponseBody
    public R delLinks(@RequestParam("infoshareIds[]") Long[] infoshareIds) throws Exception {
        //删除数据库相关配置信息
        infoshareService.removeLink(infoshareIds);
        //删除job
        for (Long infoshareId : infoshareIds) {
            jobConfigurationService.delJob(infoshareId);
        }
        return R.ok(true);
    }

    /**
     * 复制链路
     */
    @RequestMapping("/copyLinks")
    @ResponseBody
    public R copyLinks(@RequestParam("infoshareIds[]") Long[] infoshareIds) throws Exception {
        infoshareService.updateCopyLinks(infoshareIds);
        return R.ok(true);
    }

    /**
     * 触发全量
     */
    @RequestMapping("/triggerAll")
    @ResponseBody
    public R triggerAll(@RequestParam("infoshareId") Long infoshareId) throws Exception {
        jobConfigurationService.triggerAll(infoshareId);
        return R.ok(true);
    }

    /**
     * 暂停任务管理器
     */
    @RequestMapping("/closeSch")
    @ResponseBody
    public R closeSch(@RequestParam("oper") String oper) {
        CommonUtil commonUtil = CommonUtil.getInstance();
        String host = commonUtil.getHostShortName();
        checkConnMapper.updateOper(host, oper);
        return R.ok(commonUtil.closeSch());
    }

    /**
     * 开始任务管理器
     */
    @RequestMapping("/openSch")
    @ResponseBody
    public R openSch(@RequestParam("oper") String oper) {
        CommonUtil commonUtil = CommonUtil.getInstance();
        String host = commonUtil.getHostShortName();
        checkConnMapper.updateOper(host, oper);
        return R.ok(commonUtil.openSch());
    }

    /**
     * 获取服务器的默认地址
     */
    @RequestMapping("/getDefaultURL")
    @ResponseBody
    public R getDefaultURL(@RequestParam("serviceType") String serviceType) {
        if (PropertyNameConstants.ACTIVE_MQ.equals(serviceType)) {
            return R.ok(configConstants.getDefalutActviemqURL());
        } else if (PropertyNameConstants.IBM_MQ.equals(serviceType)) {
            return R.ok(configConstants.getDefalutIbmmqURL());
        } else {
            return R.ok("");
        }
    }
}
