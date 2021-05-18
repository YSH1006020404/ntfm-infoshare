package cn.les.ntfm.infoshareConf.service.impl;

import cn.les.framework.web.base.impl.BaseServiceImpl;
import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.infoshare.entity.InfoshareConfigDO;
import cn.les.ntfm.infoshare.entity.XmlTemplateDO;
import cn.les.ntfm.infoshare.dao.*;
import cn.les.ntfm.infoshareConf.service.XmlTemplateService;
import cn.les.ntfm.util.Log4jUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service("xmlTemplateService")
public class XmlTemplateServiceImpl extends BaseServiceImpl implements XmlTemplateService {
    @Resource
    private XmlTemplateMapper xmlTemplateMapper;

    @Override
    public List<XmlTemplateDO> listXmlTemplate(Long id) {
        return xmlTemplateMapper.listXmlTemplate(id);
    }

    @Override
    public List<Map<Long, String>> listJsonData(Long id) {
        return xmlTemplateMapper.listJsonData(id);
    }

    @Override
    public XmlTemplateDO getXmlTemplateById(Long id) {
        return xmlTemplateMapper.getDataById(id);
    }

    @Override
    public void saveXmlTemplate(XmlTemplateDO xmlTemplateDO) throws Exception {
        try {
            xmlTemplateMapper.addData(xmlTemplateDO);
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("XmlTemplateServiceImpl类的saveXmlTemplate方法出现错误，错误原因：", e);
            throw new Exception("新增XML模板失败！详情请查看日志！");
        }
    }

    @Override
    public void saveCopyXmlTemplate(Long[] ids) throws Exception {
        try {
            for (Long id : ids) {
                //检索源XML格式
                List<XmlTemplateDO> xmlTemplateDOList = xmlTemplateMapper.listXmlTemplate(id);
                //key值为源id，value为新id
                Map<Long, Long> xmlIdMap = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
                // 复制新XML格式
                if (xmlTemplateDOList != null && xmlTemplateDOList.size() > 0) {
                    Long sourceId;
                    for (XmlTemplateDO xmlTemplateDO : xmlTemplateDOList) {
                        sourceId = xmlTemplateDO.getId();
                        if (xmlTemplateDO.getPid() == null) {
                            xmlTemplateMapper.addData(xmlTemplateDO);
                            xmlIdMap.put(sourceId, xmlTemplateDO.getId());
                        } else {
                            xmlTemplateDO.setPid(xmlIdMap.get(xmlTemplateDO.getPid()));
                            xmlTemplateMapper.addData(xmlTemplateDO);
                            xmlIdMap.put(sourceId, xmlTemplateDO.getId());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("XmlTemplateServiceImpl类的saveCopyXmlTemplate方法出现错误，错误原因：", e);
            throw new Exception("复制XML模板失败！详情请查看日志！");
        }
    }

    @Override
    public void removeXmlTemplate(Long[] ids) throws Exception {
        try {
            for (Long id : ids) {
                xmlTemplateMapper.deleteDataByPid(id);
            }
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("XmlTemplateServiceImpl类的removeXmlTemplate方法出现错误，错误原因：", e);
            throw new Exception("删除XML模板失败！详情请查看日志！");
        }
    }

    @Override
    public void updateXmlTemplate(XmlTemplateDO xmlTemplateDO) throws Exception {
        try {
            xmlTemplateMapper.updateDataById(xmlTemplateDO);
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("XmlTemplateServiceImpl类的updateXmlTemplate方法出现错误，错误原因：", e);
            throw new Exception("更新XML模板失败！详情请查看日志！");
        }
    }

    @Override
    public List<InfoshareConfigDO> listAffectedLinks(Long id) {
        return xmlTemplateMapper.listAffectedLinks(id);
    }


}


