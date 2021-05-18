package cn.les.ntfm.infoshareConf.service;


import cn.les.ntfm.infoshare.entity.InfoshareConfigDO;
import cn.les.ntfm.infoshare.entity.XmlTemplateDO;

import java.util.List;
import java.util.Map;

public interface XmlTemplateService {
    List<XmlTemplateDO> listXmlTemplate(Long id);

    List<Map<Long, String>> listJsonData(Long id);

    XmlTemplateDO getXmlTemplateById(Long id);

    void saveXmlTemplate(XmlTemplateDO xmlTemplateDO) throws Exception;

    void saveCopyXmlTemplate(Long[] ids) throws Exception;

    void removeXmlTemplate(Long[] ids) throws Exception;

    void updateXmlTemplate(XmlTemplateDO xmlTemplateDO) throws Exception;

    /**
     * 获取已关联该模板的链路
     *
     * @param id
     * @return
     */
    List<InfoshareConfigDO> listAffectedLinks(Long id);
}
