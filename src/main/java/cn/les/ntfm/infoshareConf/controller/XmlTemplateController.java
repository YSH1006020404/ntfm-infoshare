package cn.les.ntfm.infoshareConf.controller;

import cn.les.framework.core.result.R;
import cn.les.framework.web.base.impl.BaseControllerImpl;
import cn.les.ntfm.infoshare.entity.XmlTemplateDO;
import cn.les.ntfm.infoshareConf.service.XmlTemplateService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/infoshareConf/xmlTemplate")
public class XmlTemplateController extends BaseControllerImpl {
    @Resource
    private XmlTemplateService xmlTemplateService;

    /**
     * XML格式配置管理页
     */
    @RequestMapping("/queryXmlTemplate")
    @ResponseBody
    public R queryXmlTemplate(@RequestParam("id") Long id) {
        return R.ok(xmlTemplateService.listXmlTemplate(id));
    }

    /**
     * XML格式配置新增
     */
    @RequestMapping("/addXmlTemplate")
    @ResponseBody
    public R addXmlTemplate(XmlTemplateDO xmlFormatConfigDO) throws Exception {
        xmlTemplateService.saveXmlTemplate(xmlFormatConfigDO);
        return R.ok(true);
    }

    /**
     * XML格式配置删除
     */
    @RequestMapping("/deleteXmlTemplate")
    @ResponseBody
    public R deleteXmlTemplate(@RequestParam("ids[]") Long[] ids) throws Exception {
        xmlTemplateService.removeXmlTemplate(ids);
        return R.ok(true);
    }

    /**
     * XML格式配置编辑
     */
    @RequestMapping("/editXmlTemplate")
    @ResponseBody
    public R editXmlTemplate(XmlTemplateDO xmlFormatConfigDO) throws Exception {
        xmlTemplateService.updateXmlTemplate(xmlFormatConfigDO);
        return R.ok(true);
    }

    /**
     * XML格式复制
     */
    @RequestMapping("/copyXmlTemplate")
    @ResponseBody
    public R copyXmlTemplate(@RequestParam("ids[]") Long[] ids) throws Exception {
        xmlTemplateService.saveCopyXmlTemplate(ids);
        return R.ok(true);
    }

    /**
     * XML格式主键检索
     */
    @RequestMapping("/getXmlTemplateById")
    @ResponseBody
    public R getXmlTemplateById(@RequestParam("id") Long id) {
        return R.ok(xmlTemplateService.getXmlTemplateById(id));
    }

    /**
     * 获取已关联该模板的链路
     */
    @RequestMapping("/listAffectedLinks")
    @ResponseBody
    public R listAffectedLinks(@RequestParam("id") Long id) {
        return R.ok(xmlTemplateService.listAffectedLinks(id));
    }

    @RequestMapping("/queryXMLName")
    @ResponseBody
    public R queryXMLName() {
        return R.ok(xmlTemplateService.listJsonData(null));
    }

}


