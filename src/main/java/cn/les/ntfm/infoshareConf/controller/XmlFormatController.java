package cn.les.ntfm.infoshareConf.controller;

import cn.les.framework.core.result.R;
import cn.les.framework.web.base.impl.BaseControllerImpl;
import cn.les.ntfm.infoshare.entity.InfoshareConfigDO;
import cn.les.ntfm.infoshare.entity.XmlFormatConfigDO;
import cn.les.ntfm.infoshare.service.JobConfigurationService;
import cn.les.ntfm.infoshareConf.service.XmlFormatService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * 输出项配置
 *
 * @author 杨硕
 * @date 2020-06-03-下午2:51
 */

@Controller
@RequestMapping("/infoshareConf/xmlFormat")
public class XmlFormatController extends BaseControllerImpl {
    @Resource
    private XmlFormatService xmlFormatService;
    @Resource
    private JobConfigurationService jobConfigurationService;

    /**
     * 新增XML配置信息
     */
    @RequestMapping("/saveXmlFormat")
    @ResponseBody
    public R saveXmlFormat(@RequestBody JSONObject data) throws Exception {
        XmlFormatConfigDO xmlFormatConfigDO = xmlFormatService.saveXmlFormat(data);
        //修改job
        InfoshareConfigDO infoshareConfigDO = xmlFormatService.getInfoshareByXmlformatId(xmlFormatConfigDO.getId());
        jobConfigurationService.delJob(infoshareConfigDO.getId());
        jobConfigurationService.addJob(infoshareConfigDO.getId());
        return R.ok(true);
    }

    /**
     * 修改XML配置信息
     */
    @RequestMapping("/updateXmlFormat")
    @ResponseBody
    public R updateXmlFormat(@RequestBody JSONObject data) throws Exception {
        XmlFormatConfigDO xmlFormatConfigDO = xmlFormatService.updateXmlFormat(data);
        //修改job
        InfoshareConfigDO infoshareConfigDO = xmlFormatService.getInfoshareByXmlformatId(xmlFormatConfigDO.getId());
        jobConfigurationService.delJob(infoshareConfigDO.getId());
        jobConfigurationService.addJob(infoshareConfigDO.getId());
        return R.ok(true);
    }

    /**
     * 删除XML配置
     */
    @RequestMapping("/removeXmlFormat")
    @ResponseBody
    public R removeXmlFormat(@RequestParam("ids[]") Long[] ids) throws Exception {
        InfoshareConfigDO infoshareConfigDO = xmlFormatService.getInfoshareByXmlformatId(ids[0]);
        xmlFormatService.removeXmlFormat(ids);
        //修改job
        jobConfigurationService.delJob(infoshareConfigDO.getId());
        jobConfigurationService.addJob(infoshareConfigDO.getId());
        return R.ok(true);
    }

    /**
     * 主键检索XML配置
     */
    @RequestMapping("/getXmlFormatById")
    @ResponseBody
    public R getXmlFormatById(@RequestParam("id") Long id) {
        return R.ok(xmlFormatService.getXmlFormatById(id));
    }

    /**
     * 根据链路ID检索XML配置信息
     */
    @RequestMapping("/listXmlFormatConfig")
    @ResponseBody
    public R getDataBaseConfigByInfoshareId(@RequestParam("xmlFormatConfigId") Long xmlFormatConfigId) {
        List<XmlFormatConfigDO> result = xmlFormatService.listXmlFormatConfig(xmlFormatConfigId);
        return R.ok(result);
    }

    /**
     * 根据父节点获取可选择的XML标签类型
     */
    @RequestMapping("/getLableTypeByPid")
    @ResponseBody
    public R getLableTypeByPid(@RequestParam("pid") Long pid) {
        return R.ok(xmlFormatService.getLableTypeByPid(pid));
    }

    /**
     * 判断是否可以增加子节点
     */
    @RequestMapping("/getAddXmlFormatConfigAbility")
    @ResponseBody
    public R getAddXmlFormatConfigAbility(@RequestParam("pid") Long pid) {
        return R.ok(xmlFormatService.getAddXmlFormatConfigAbility(pid));
    }

    /**
     * 根据父键获取关联表信息
     */
    @RequestMapping("/getTableInfoByPid")
    @ResponseBody
    public R getTableInfoByPid(@RequestParam("pid") Long pid) {
        return R.ok(xmlFormatService.getTableInfoByPid(pid));
    }

    /**
     * 获取可用的数据源表
     */
    @RequestMapping("/listAvailableTables")
    @ResponseBody
    public R listAvailableTables(@RequestParam("databaseConfigId") Long databaseConfigId, @RequestParam("labelType") String labelType) {
        return R.ok(xmlFormatService.listAvailableTables(databaseConfigId, labelType));
    }
}
