package cn.les.ntfm.infoshareConf.controller;

import cn.les.framework.core.result.R;
import cn.les.framework.web.base.impl.BaseControllerImpl;
import cn.les.ntfm.infoshare.entity.InfoshareConfigDO;
import cn.les.ntfm.infoshare.service.JobConfigurationService;
import cn.les.ntfm.infoshareConf.service.DataSourceConfigService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.quartz.CronExpression;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 数据源配置Controller
 *
 * @author 杨硕
 * @date 2020-06-08-上午10:54
 */

@Controller
@RequestMapping("/infoshareConf/dataSourceConfig")
public class DataSourceConfigController extends BaseControllerImpl {
    @Resource
    private DataSourceConfigService dataSourceConfigService;
    @Resource
    private JobConfigurationService jobConfigurationService;

    /**
     * 通过infoshareId查询数据源配置信息
     */
    @RequestMapping("/getDataByInfoshareId")
    @ResponseBody
    public R getDataByInfoshareId(@RequestParam("infoshareId") Long infoshareId) {
        return R.ok(dataSourceConfigService.getDataByInfoshareId(infoshareId));
    }

    /**
     * 新增数据源
     */
    @RequestMapping("/saveDataBaseConfig")
    @ResponseBody
    public R saveDataBaseConfig(@RequestBody JSONObject data) throws Exception {
        dataSourceConfigService.saveDataBaseConfig(data);
        //修改job
        InfoshareConfigDO infoshareConfigDO = JSON.parseObject(JSON.toJSONString(data.get("infoshareConfig")), InfoshareConfigDO.class);
        jobConfigurationService.delJob(infoshareConfigDO.getId());
        jobConfigurationService.addJob(infoshareConfigDO.getId());
        return R.ok(true);
    }


    /**
     * 修改数据源
     */
    @RequestMapping("/updateDataBaseConfig")
    @ResponseBody
    public R updateDataBaseConfig(@RequestBody JSONObject data) throws Exception {
        dataSourceConfigService.updateDataBaseConfig(data);
        //修改job
        InfoshareConfigDO infoshareConfigDO = JSON.parseObject(JSON.toJSONString(data.get("infoshareConfig")), InfoshareConfigDO.class);
        jobConfigurationService.delJob(infoshareConfigDO.getId());
        jobConfigurationService.addJob(infoshareConfigDO.getId());
        return R.ok(true);
    }

    /**
     * 校验筛选条件
     */
    @RequestMapping("/validateCondition")
    @ResponseBody
    public R validateCondition(@RequestParam("tableName") String tableName,
                               @RequestParam("condition") String condition) {
        return R.ok(dataSourceConfigService.validateCondition(tableName, condition));
    }

    /**
     * 校验关联条件
     */
    @RequestMapping("/validateRelations")
    @ResponseBody
    public R validateRelations(@RequestBody JSONObject dataSource) throws Exception {
        return R.ok(dataSourceConfigService.validateRelations(dataSource));
    }

    /**
     * 校验cron表达式
     */
    @RequestMapping("/validateCronExpression")
    @ResponseBody
    public R validateCronExpression(@RequestParam("cronExpression") String cronExpression) {
        if (StringUtils.isNotEmpty(cronExpression)) {
            if (CronExpression.isValidExpression(cronExpression)) {
                return R.ok(true);
            } else {
                return R.ok(false);
            }
        } else {
            return R.ok(true);
        }
    }

    /**
     * 获取默认数据接收方式（数据库）
     */
    @RequestMapping("/getDefaultSourceType")
    @ResponseBody
    public R getDefaultSourceType() {
        return R.ok(dataSourceConfigService.getDefaultSourceType());
    }
}
