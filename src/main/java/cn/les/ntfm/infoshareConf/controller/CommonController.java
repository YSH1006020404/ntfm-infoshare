package cn.les.ntfm.infoshareConf.controller;

import cn.les.framework.core.result.R;
import cn.les.framework.web.base.impl.BaseControllerImpl;
import cn.les.ntfm.infoshareConf.service.CommonService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;


@Controller
@RequestMapping("/infoshareConf/common")
public class CommonController extends BaseControllerImpl {
    @Resource
    private CommonService commonService;

    /**
     * 检索表的所有字段
     */
    @RequestMapping("/listColumns")
    @ResponseBody
    public R listColumns(@RequestParam("tableId") Long tableId) {
        return R.ok(commonService.listColumns(tableId));
    }


    /**
     * 获取所有可输出数据表
     */
    @RequestMapping("/listOutputTable")
    @ResponseBody
    public R listOutputTable() {
        return R.ok(commonService.listOutputTable());
    }
}


