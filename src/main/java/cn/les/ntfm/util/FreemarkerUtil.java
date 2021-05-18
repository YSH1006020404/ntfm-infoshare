package cn.les.ntfm.util;

import cn.les.framework.core.util.ExceptionUtil;
import cn.les.framework.zlmq.util.InitUtil;
import cn.les.ntfm.constant.ConfigConstants;
import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.infoshare.entity.XmlFormatConfigDO;
import cn.les.ntfm.infoshare.dao.OutputRelationMapper;
import cn.les.ntfm.infoshare.dao.XmlFormatConfigMapper;
import cn.les.ntfm.infoshare.dto.JobConfiguration;
import com.google.common.base.CaseFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @create 2020-04-23-下午4:48
 */
@Component
public class FreemarkerUtil {
    @Resource
    private ConfigConstants configConstants;
    @Resource
    private OutputRelationMapper outputRelationMapper;
    @Resource
    private XmlFormatConfigMapper xmlFormatConfigMapper;
    private static FreemarkerUtil freemarkerUtil;

    @PostConstruct
    public void init() {
        freemarkerUtil = this;
        freemarkerUtil.configConstants = this.configConstants;
        freemarkerUtil.outputRelationMapper = this.outputRelationMapper;
        freemarkerUtil.xmlFormatConfigMapper = this.xmlFormatConfigMapper;
    }

    /**
     * 获取freemarker模板
     *
     * @param filePath 文件路径
     * @param linkId   链路主键
     * @return Template
     * @throws IOException
     */
    public static Template getTemplate(String filePath, Long linkId) throws IOException {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setDirectoryForTemplateLoading(new File(filePath));
        Template t = configuration.getTemplate(InitUtil.getREGION() + Constants.UNDERLINE + linkId + Constants.FILE_FTL_SUFFIX);
        return t;
    }

    /**
     * 生成freemarker模板
     *
     * @param jobConfiguration  job配置
     * @param xmlformatconfigId xml模板ID
     */
    public static void createFtlFiles(JobConfiguration jobConfiguration, Long xmlformatconfigId) throws IOException {
        //生成xml
        Document document = DocumentHelper.createDocument();
        XmlFormatConfigDO rootXmlFormatConfigDO = freemarkerUtil.xmlFormatConfigMapper.getDataById(xmlformatconfigId);
        Element root = document.addElement(rootXmlFormatConfigDO.getLabelName());
        List<XmlFormatConfigDO> xmlFormatConfigList = freemarkerUtil.xmlFormatConfigMapper.listByPid(xmlformatconfigId, null);
        if (xmlFormatConfigList != null && xmlFormatConfigList.size() > 0) {
            for (XmlFormatConfigDO xmlFormatConfig : xmlFormatConfigList) {
                recursiveXmlFormat(xmlFormatConfig, root);
            }
        }
        //生成ftl文件
        String ftl = parseToFtl(jobConfiguration, document.asXML(), xmlformatconfigId);
        FileUtil.createFile(freemarkerUtil.configConstants.getFtlPath(),
                InitUtil.getREGION() + Constants.UNDERLINE + jobConfiguration.getLinkID() + Constants.FILE_FTL_SUFFIX,
                ftl);

        Template template = getTemplate(freemarkerUtil.configConstants.getFtlPath(), jobConfiguration.getLinkID());
        Constants.ftlTemplateMap.put(jobConfiguration.getLinkID(), template);
        Date updateTime = freemarkerUtil.xmlFormatConfigMapper.getFreemarkerUpdateTime(xmlformatconfigId);
        Constants.ftlTemplateUpdateTimeMap.put(jobConfiguration.getLinkID(), updateTime);
        jobConfiguration.setFtlTemplateUpdateTime(updateTime);
    }


    /**
     * 根据XML配置生成XML文件
     */
    private static void recursiveXmlFormat(XmlFormatConfigDO xmlFormatConfigDO, Element element) {
        switch (xmlFormatConfigDO.getLabelType()) {
            case Constants.XML_COLUMN_TYPE_TXT:
                if (StringUtils.isEmpty(xmlFormatConfigDO.getValue())) {
                    element.addElement(xmlFormatConfigDO.getLabelName());
                } else {
                    element.addElement(xmlFormatConfigDO.getLabelName())
                            .setText("${\"" + xmlFormatConfigDO.getValue() + "\"!}");
                }
                break;
            case Constants.XML_COLUMN_TYPE_SUBTYPE:
                element.addElement(xmlFormatConfigDO.getLabelName())
                        .setText("${" + xmlFormatConfigDO.getLabelName() + "!\"" + xmlFormatConfigDO.getValue() + "\"}");
                break;
            case Constants.XML_COLUMN_TYPE_YYYYMMDDHHMMSS:
            case Constants.XML_COLUMN_TYPE_SERIAL:
            case Constants.XML_COLUMN_TYPE_STATISTICS:
                element.addElement(xmlFormatConfigDO.getLabelName())
                        .setText("${" + xmlFormatConfigDO.getLabelName() + "!}");
                break;
            case Constants.XML_COLUMN_TYPE_DBCOLUMN:
                String columnName = freemarkerUtil.outputRelationMapper.getDataById(xmlFormatConfigDO.getOutputrelationId()).getColumnName();
                String dataItemName = freemarkerUtil.xmlFormatConfigMapper.getDataItemById(xmlFormatConfigDO.getId()).getLabelName();
                String columnType = freemarkerUtil.outputRelationMapper.getDataById(xmlFormatConfigDO.getOutputrelationId()).getColumnType();
                if (Constants.DATE_TYPE_CLOB.equals(columnType)
                        && (Constants.TRN_TRFD.equals(dataItemName) || Constants.TRN_EXPD.equals(dataItemName))) {
                    element.addCDATA("<#noescape>"
                            + "${" + dataItemName + "." + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, columnName) + "!}"
                            + "</#noescape>");
                } else {
                    element.addElement(xmlFormatConfigDO.getLabelName())
                            .setText("${" + dataItemName + "." + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, columnName) + "!}");
                }
                break;
            case Constants.XML_COLUMN_TYPE_ROOTDATALIST:
            case Constants.XML_COLUMN_TYPE_DATALIST:
            case Constants.XML_COLUMN_TYPE_DATAITEM:
            case Constants.XML_COLUMN_TYPE_OTHER:
                element.addElement(xmlFormatConfigDO.getLabelName());
                break;
            default:
                break;
        }
        List<XmlFormatConfigDO> xmlFormatConfigList = freemarkerUtil.xmlFormatConfigMapper.listByPid(xmlFormatConfigDO.getId(), null);
        if (xmlFormatConfigList != null && xmlFormatConfigList.size() > 0) {
            for (XmlFormatConfigDO xmlFormatConfig : xmlFormatConfigList) {
                recursiveXmlFormat(xmlFormatConfig, element.element(xmlFormatConfigDO.getLabelName()));
            }
        }
    }

    /**
     * XML格式转换为FTL格式
     *
     * @param xmlStr            XML内容
     * @param xmlFormatConfigId XML根节点
     * @return String
     */
    private static String parseToFtl(JobConfiguration jobConfiguration, String xmlStr, Long xmlFormatConfigId) {
        StringBuilder xmlStringBuilder = new StringBuilder(xmlStr);
        //添加转义
        xmlStringBuilder.insert(0, "<#escape x as x?xml>");
        xmlStringBuilder.append("</#escape>");

        //添加基础表配置
        //LIST标签（基础表）
        XmlFormatConfigDO rootDataListParam = new XmlFormatConfigDO();
        rootDataListParam.setId(xmlFormatConfigId);
        rootDataListParam.setLabelType(Constants.XML_COLUMN_TYPE_ROOTDATALIST);
        List<XmlFormatConfigDO> rootDataListXmlFormatConfig = freemarkerUtil.xmlFormatConfigMapper.listXmlFormatConfig(rootDataListParam);
        if (rootDataListXmlFormatConfig == null || rootDataListXmlFormatConfig.size() != 1) {
            ExceptionUtil.throwBusinessEx("XML必须且只能配置一个ROOTDATALIST！");
            return null;
        }
        String rootDataListLabel = rootDataListXmlFormatConfig.get(0).getLabelName();
        //LIST标签（基础表）
        jobConfiguration.setRootDataList(rootDataListLabel);
        //单条记录标签（基础表）
        List<XmlFormatConfigDO> rootDataItemXmlFormatConfig = freemarkerUtil.xmlFormatConfigMapper.listByPid(
                rootDataListXmlFormatConfig.get(0).getId(), Constants.XML_COLUMN_TYPE_DATAITEM);
        if (rootDataItemXmlFormatConfig == null || rootDataItemXmlFormatConfig.size() != 1) {
            ExceptionUtil.throwBusinessEx("ROOTDATALIST必须且只能配置一个DATAITEM！");
            return null;
        }
        String rootDataItemLabel = rootDataItemXmlFormatConfig.get(0).getLabelName();
        setFtlList(xmlStringBuilder, rootDataListLabel, rootDataItemLabel);

        //添加关联表配置
        XmlFormatConfigDO relationDataListParam = new XmlFormatConfigDO();
        relationDataListParam.setId(xmlFormatConfigId);
        relationDataListParam.setLabelType(Constants.XML_COLUMN_TYPE_DATALIST);
        List<XmlFormatConfigDO> relationDataListXmlFormatConfigList = freemarkerUtil.xmlFormatConfigMapper.listXmlFormatConfig(relationDataListParam);
        if (relationDataListXmlFormatConfigList != null && relationDataListXmlFormatConfigList.size() > 0) {
            for (XmlFormatConfigDO relationDataListXmlFormatConfig : relationDataListXmlFormatConfigList) {
                //单条记录标签（关联表）
                List<XmlFormatConfigDO> relationDataItemXmlFormatConfig = freemarkerUtil.xmlFormatConfigMapper.listByPid(
                        relationDataListXmlFormatConfig.getId(), Constants.XML_COLUMN_TYPE_DATAITEM);
                if (relationDataItemXmlFormatConfig == null || relationDataItemXmlFormatConfig.size() != 1) {
                    ExceptionUtil.throwBusinessEx("DATALIST必须且只能配置一个DATAITEM！");
                    return null;
                }
                String relationDataItemLabel = relationDataItemXmlFormatConfig.get(0).getLabelName();
                setFtlRelationTableList(xmlStringBuilder
                        , rootDataItemLabel
                        , relationDataListXmlFormatConfig.getLabelName()
                        , relationDataItemLabel);
            }
        }
        return xmlStringBuilder.toString();
    }

    /**
     * 关联表LIST标签处理
     *
     * @param xmlStringBuilder      原XML配置
     * @param rootDataItemLabel     单条记录标签（基础表）
     * @param relationDataListLabel LIST标签（关联表）
     * @param relationDataItemLabel 单条记录标签（关联表）
     */
    private static void setFtlRelationTableList(StringBuilder xmlStringBuilder, String rootDataItemLabel, String relationDataListLabel, String relationDataItemLabel) {
        //关联表参数配置
        int insertIndex = xmlStringBuilder.indexOf("<" + relationDataListLabel + ">");
        xmlStringBuilder.insert(insertIndex, "<#assign " + relationDataListLabel + "=" + rootDataItemLabel + "." + relationDataListLabel + "/>");
        setFtlList(xmlStringBuilder, relationDataListLabel, relationDataItemLabel);
    }

    /**
     * LIST标签处理
     *
     * @param xmlStringBuilder 原XML配置
     * @param dataListLabel    LIST标签
     * @param dataItemLabel    单条记录标签
     */
    private static void setFtlList(StringBuilder xmlStringBuilder, String dataListLabel, String dataItemLabel) {
        //LIST是否有值配置start
        int insertIndex = xmlStringBuilder.indexOf("<" + dataListLabel + ">");
        xmlStringBuilder.insert(insertIndex, "<#if " + dataListLabel + "??>");
        //LIST别名配置start
        insertIndex = xmlStringBuilder.indexOf("<" + dataItemLabel + ">");
        xmlStringBuilder.insert(insertIndex, "<#list " + dataListLabel + " as " + dataItemLabel + ">");
        //LIST别名配置end
        insertIndex = xmlStringBuilder.indexOf("</" + dataListLabel + ">");
        xmlStringBuilder.insert(insertIndex, "</#list>");
        //LIST是否有值配置end
        insertIndex = xmlStringBuilder.indexOf("</" + dataListLabel + ">") + dataListLabel.length() + 3;
        xmlStringBuilder.insert(insertIndex, "</#if>");
    }

}
