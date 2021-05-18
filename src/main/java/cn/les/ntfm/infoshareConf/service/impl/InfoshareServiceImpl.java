package cn.les.ntfm.infoshareConf.service.impl;

import cn.les.framework.core.pagination.PageResult;
import cn.les.framework.web.base.impl.BaseServiceImpl;
import cn.les.ntfm.constant.Constants;
import cn.les.ntfm.constant.LogConstants;
import cn.les.ntfm.constant.PropertyNameConstants;
import cn.les.ntfm.infoshare.entity.*;
import cn.les.ntfm.infoshare.dao.*;
import cn.les.ntfm.infoshareConf.service.DataSourceConfigService;
import cn.les.ntfm.infoshareConf.service.InfoshareService;
import cn.les.ntfm.util.Log4jUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("infoshareService")
public class InfoshareServiceImpl extends BaseServiceImpl implements InfoshareService {
    @Resource
    private DatabaseConfigMapper databaseConfigMapper;
    @Resource
    private DatabaseRelationMapper databaseRelationMapper;
    @Resource
    private OutputRelationMapper outputRelationMapper;
    @Resource
    private XmlFormatConfigMapper xmlFormatConfigMapper;
    @Resource
    private ActivemqConfigMapper activemqConfigMapper;
    @Resource
    private IbmmqConfigMapper ibmmqConfigMapper;
    @Resource
    private FtpConfigMapper ftpConfigMapper;
    @Resource
    private TcpipConfigMapper tcpipConfigMapper;
    @Resource
    private PropertyDictMapper propertyDictMapper;
    @Resource
    private DestinationConfigRelationMapper destinationConfigRelationMapper;
    @Resource
    private InfoShareConfigMapper infoShareConfigMapper;
    @Resource
    private CommonMapper commonMapper;
    @Resource
    private DataSourceConfigService dataSourceConfigService;

    @Override
    public PageResult<Map<String, Object>> listMsgs(Map<String, Object> params) {
        int pageIndex = Integer.parseInt(String.valueOf(params.get("pageIndex")));
        int pageSize = Integer.parseInt(String.valueOf(params.get("pageSize")));
        params.put("rownumFrom", pageIndex * pageSize);
        params.put("rownumTo", (pageIndex + 1) * pageSize);
        List<Map<String, Object>> data = infoShareConfigMapper.queryMsgs(params);
        Integer total = infoShareConfigMapper.queryMsgsCount(params);

        PageResult result = new PageResult();
        result.setData(data);
        result.setTotal(total);
        result.setPageSize(pageSize);
        result.setPage(pageIndex);
        return result;
    }

    @Override
    public void saveEditMsgs(JSONObject data) throws Exception {
        try {
            //新增infoshareconfig
            InfoshareConfigDO infoshareConfigDO = JSON.parseObject(JSON.toJSONString(data.get("infoshareConfig")), InfoshareConfigDO.class);
            infoShareConfigMapper.addData(infoshareConfigDO);

            //新增输出配置
            saveDestinations(infoshareConfigDO.getId(), data);
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("EditLinkServiceImpl类的saveEditMsgs方法出现错误，错误原因：", e);
            throw new Exception("新增失败！详情请查看日志！");
        }
    }

    @Override
    public void updateEditMsgs(JSONObject data) throws Exception {
        InfoshareConfigDO infoshareConfigDO = JSON.parseObject(JSON.toJSONString(data.get("infoshareConfig")), InfoshareConfigDO.class);
        //乐观锁校验
        InfoshareConfigDO oldInfoshareConfig = infoShareConfigMapper.getDataById(infoshareConfigDO.getId());
        if (oldInfoshareConfig == null
                || !infoshareConfigDO.getUpdateTime().equals(oldInfoshareConfig.getUpdateTime())) {
            throw new Exception("页面已过期，请刷新页面！");
        }

        try {
            //修改infoshareconfig
            infoShareConfigMapper.updateDataById(infoshareConfigDO);
            infoShareConfigMapper.getDataById(infoshareConfigDO.getId());
            //更新输出配置
            saveDestinations(infoshareConfigDO.getId(), data);
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("EditLinkServiceImpl类的updateEditMsgs方法出现错误，错误原因：", e);
            throw new Exception("更新失败！详情请查看日志！");
        }
    }

    @Override
    public Map<String, Object> getMsgsById(long linkId) {
        Map<String, Object> result = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
        //infoshareconfig
        InfoshareConfigDO infoshareConfigDO = infoShareConfigMapper.getDataById(linkId);
        result.put("infoshareConfig", infoshareConfigDO);
        //输出项配置
        DestinationConfigDO destinationParam = new DestinationConfigDO();
        destinationParam.setInfoshareconfigId(linkId);

        //输出方式为activemq
        List<ActiveMqConfigDO> activeMqConfigDOList = new ArrayList<>();
        destinationParam.setDestinationType(Constants.ACTIVEMQCONFIG);
        List<DestinationConfigDO> destinationConfigList = destinationConfigRelationMapper.listData(destinationParam);
        if (destinationConfigList != null && destinationConfigList.size() > 0) {
            for (DestinationConfigDO destinationConfigDO : destinationConfigList) {
                ActiveMqConfigDO activeMqConfigDO = activemqConfigMapper.getDataById(destinationConfigDO.getDestinationId());
                activeMqConfigDO.setDestinationConfigDO(destinationConfigDO);
                activeMqConfigDOList.add(activeMqConfigDO);
            }
        }
        result.put(Constants.ACTIVEMQCONFIG, activeMqConfigDOList);

        //输出方式为ibmmq
        List<IbmMqConfigDO> ibmMqConfigDOList = new ArrayList<>();
        destinationParam.setDestinationType(Constants.IBMMQCONFIG);
        destinationConfigList = destinationConfigRelationMapper.listData(destinationParam);
        if (destinationConfigList != null && destinationConfigList.size() > 0) {
            for (DestinationConfigDO destinationConfigDO : destinationConfigList) {
                IbmMqConfigDO ibmMqConfigDO = ibmmqConfigMapper.getDataById(destinationConfigDO.getDestinationId());
                ibmMqConfigDO.setDestinationConfigDO(destinationConfigDO);
                ibmMqConfigDOList.add(ibmMqConfigDO);
            }
        }
        result.put(Constants.IBMMQCONFIG, ibmMqConfigDOList);

        //输出方式为ftp
        List<FtpConfigDO> ftpConfigDOList = new ArrayList<>();
        destinationParam.setDestinationType(Constants.FTPCONFIG);
        destinationConfigList = destinationConfigRelationMapper.listData(destinationParam);
        if (destinationConfigList != null && destinationConfigList.size() > 0) {
            for (DestinationConfigDO destinationConfigDO : destinationConfigList) {
                FtpConfigDO ftpConfigDO = ftpConfigMapper.getDataById(destinationConfigDO.getDestinationId());
                ftpConfigDO.setDestinationConfigDO(destinationConfigDO);
                ftpConfigDOList.add(ftpConfigDO);
            }
        }
        result.put(Constants.FTPCONFIG, ftpConfigDOList);

        //输出方式为tcpip
        List<TcpipConfigDO> tcpipConfigDOList = new ArrayList<>();
        destinationParam.setDestinationType(Constants.TCPIPCONFIG);
        destinationConfigList = destinationConfigRelationMapper.listData(destinationParam);
        if (destinationConfigList != null && destinationConfigList.size() > 0) {
            for (DestinationConfigDO destinationConfigDO : destinationConfigList) {
                TcpipConfigDO tcpipConfigDO = tcpipConfigMapper.getDataById(destinationConfigDO.getDestinationId());
                tcpipConfigDO.setDestinationConfigDO(destinationConfigDO);
                tcpipConfigDOList.add(tcpipConfigDO);
            }
        }
        result.put(Constants.TCPIPCONFIG, tcpipConfigDOList);

        return result;
    }

    @Override
    public void updateCopyLinks(Long[] infoshareIds) throws Exception {
        try {
            for (Long infoshareId : infoshareIds) {
                InfoshareConfigDO infoshareConfigOld = infoShareConfigMapper.getDataById(infoshareId);
                //复制后的数据源主键
                Long sourceIdNew = null;
                if (infoshareConfigOld.getSourceId() != null) {
                    DataBaseConfigDO dataBaseConfigOld = databaseConfigMapper.getDataById(infoshareConfigOld.getSourceId());
                    if (dataBaseConfigOld != null) {
                        //复制XML配置和输出字段配置
                        Long xmlformatId = recursiveXmlFormatAndOutputRelation(null, xmlFormatConfigMapper.getDataById(dataBaseConfigOld.getXmlformatconfigId()));
                        //复制数据源配置
                        DataBaseConfigDO dataBaseConfigDO = new DataBaseConfigDO();
                        BeanUtils.copyProperties(dataBaseConfigOld, dataBaseConfigDO);
                        dataBaseConfigDO.setXmlformatconfigId(xmlformatId);
                        dataBaseConfigDO.setId(null);
                        dataBaseConfigDO.setUpdateTime(null);
                        dataBaseConfigDO.setUpdateUser(null);
                        databaseConfigMapper.addData(dataBaseConfigDO);
                        sourceIdNew = dataBaseConfigDO.getId();
                        //复制关联数据源配置
                        List<DataBaseRelationDO> dataBaseRelationListOld = databaseRelationMapper.listByDatabaseconfigId(dataBaseConfigOld.getId());
                        if (dataBaseRelationListOld != null && dataBaseRelationListOld.size() > 0) {
                            for (DataBaseRelationDO dataBaseRelationOld : dataBaseRelationListOld) {
                                DataBaseRelationDO dataBaseRelationDO = new DataBaseRelationDO();
                                BeanUtils.copyProperties(dataBaseRelationOld, dataBaseRelationDO);
                                dataBaseRelationDO.setDatabaseconfigId(sourceIdNew);
                                dataBaseRelationDO.setId(null);
                                dataBaseRelationDO.setUpdateTime(null);
                                dataBaseRelationDO.setUpdateUser(null);
                                databaseRelationMapper.addData(dataBaseRelationDO);
                            }
                        }
                    }
                }

                //复制信息共享链路配置
                InfoshareConfigDO infoshareConfigDO = new InfoshareConfigDO();
                BeanUtils.copyProperties(infoshareConfigOld, infoshareConfigDO);
                infoshareConfigDO.setSourceId(sourceIdNew);
                infoshareConfigDO.setStateFlag(false);
                infoshareConfigDO.setId(null);
                infoshareConfigDO.setUpdateTime(null);
                infoshareConfigDO.setUpdateUser(null);
                infoShareConfigMapper.addData(infoshareConfigDO);

                //复制输出目的地配置
                DestinationConfigDO destinationParam = new DestinationConfigDO();
                destinationParam.setInfoshareconfigId(infoshareId);
                List<DestinationConfigDO> destinationConfigDOList = destinationConfigRelationMapper.listData(destinationParam);
                if (destinationConfigDOList != null && destinationConfigDOList.size() > 0) {
                    for (DestinationConfigDO destinationConfigDO : destinationConfigDOList) {
                        destinationConfigDO.setInfoshareconfigId(infoshareConfigDO.getId());
                        switch (destinationConfigDO.getDestinationType()) {
                            case Constants.ACTIVEMQCONFIG:
                                ActiveMqConfigDO activeMqConfigDO = activemqConfigMapper.getDataById(destinationConfigDO.getDestinationId());
                                activemqConfigMapper.addData(activeMqConfigDO);
                                destinationConfigDO.setDestinationId(activeMqConfigDO.getId());
                                destinationConfigRelationMapper.addData(destinationConfigDO);
                                break;
                            case Constants.IBMMQCONFIG:
                                IbmMqConfigDO ibmMqConfigDO = ibmmqConfigMapper.getDataById(destinationConfigDO.getDestinationId());
                                ibmmqConfigMapper.addData(ibmMqConfigDO);
                                destinationConfigDO.setDestinationId(ibmMqConfigDO.getId());
                                destinationConfigRelationMapper.addData(destinationConfigDO);
                                break;
                            case Constants.FTPCONFIG:
                                FtpConfigDO ftpConfigDO = ftpConfigMapper.getDataById(destinationConfigDO.getDestinationId());
                                ftpConfigMapper.addData(ftpConfigDO);
                                destinationConfigDO.setDestinationId(ftpConfigDO.getId());
                                destinationConfigRelationMapper.addData(destinationConfigDO);
                                break;
                            case Constants.TCPIPCONFIG:
                                TcpipConfigDO tcpipConfigDO = tcpipConfigMapper.getDataById(destinationConfigDO.getDestinationId());
                                tcpipConfigMapper.addData(tcpipConfigDO);
                                destinationConfigDO.setDestinationId(tcpipConfigDO.getId());
                                destinationConfigRelationMapper.addData(destinationConfigDO);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("InfoshareServiceImpl类的updateCopyLinks方法出现错误，错误原因：", e);
            throw new Exception("复制链路失败！详情请查看日志！");
        }
    }

    private Long recursiveXmlFormatAndOutputRelation(Long pid, XmlFormatConfigDO xmlFormatConfigOld) {
        XmlFormatConfigDO xmlFormatConfigDO = new XmlFormatConfigDO();
        BeanUtils.copyProperties(xmlFormatConfigOld, xmlFormatConfigDO);
        //复制输出字段配置
        if (xmlFormatConfigOld.getOutputrelationId() != null) {
            OutputRelationDO outputRelationOld = outputRelationMapper.getDataById(xmlFormatConfigOld.getOutputrelationId());
            outputRelationOld.setId(null);
            outputRelationOld.setCreateTime(null);
            outputRelationOld.setCreateUser(null);
            outputRelationMapper.addData(outputRelationOld);

            xmlFormatConfigDO.setOutputrelationId(outputRelationOld.getId());
        }
        //复制XML配置
        xmlFormatConfigDO.setPid(pid);
        xmlFormatConfigDO.setId(null);
        xmlFormatConfigDO.setUpdateTime(null);
        xmlFormatConfigDO.setUpdateUser(null);
        xmlFormatConfigMapper.addData(xmlFormatConfigDO);

        //递归复制XML和输出字段配置
        List<XmlFormatConfigDO> xmlFormatConfigListOld = xmlFormatConfigMapper.listByPid(xmlFormatConfigOld.getId(), null);
        if (xmlFormatConfigListOld != null && xmlFormatConfigListOld.size() > 0) {
            for (XmlFormatConfigDO xmlFormatConfig : xmlFormatConfigListOld) {
                recursiveXmlFormatAndOutputRelation(xmlFormatConfigDO.getId(), xmlFormatConfig);
            }
        }
        return xmlFormatConfigDO.getId();
    }

    @Override
    public void removeLink(Long[] infoshareIds) throws Exception {
        try {
            for (Long infoshareId : infoshareIds) {
                InfoshareConfigDO infoshareConfigDelete = infoShareConfigMapper.getDataById(infoshareId);
                //删除数据源
                if (infoshareConfigDelete.getSourceId() != null) {
                    //删除XMLFORMATCONFIG和OUTPUTRELATION
                    DataBaseConfigDO dataBaseConfigDelete = databaseConfigMapper.getDataById(infoshareConfigDelete.getSourceId());
                    if (dataBaseConfigDelete != null) {
                        if (dataBaseConfigDelete.getXmlformatconfigId() != null) {
                            XmlFormatConfigDO xmlFormatConfigDelete = new XmlFormatConfigDO();
                            xmlFormatConfigDelete.setId(dataBaseConfigDelete.getXmlformatconfigId());
                            dataSourceConfigService.removeChildrenXmlFormat(xmlFormatConfigDelete);
                        }
                        //删除DATABASRELATION
                        databaseRelationMapper.deleteDataByDatabaseconfigId(dataBaseConfigDelete.getId());
                        //删除DATABASECONFIG
                        databaseConfigMapper.deleteDataById(dataBaseConfigDelete.getId());
                    }
                }

                //删除关联的输出目的地配置表
                DestinationConfigDO destinationParam = new DestinationConfigDO();
                destinationParam.setInfoshareconfigId(infoshareId);
                List<DestinationConfigDO> destinationConfigDOList = destinationConfigRelationMapper.listData(destinationParam);
                if (destinationConfigDOList != null && destinationConfigDOList.size() > 0) {
                    for (DestinationConfigDO destinationConfigDO : destinationConfigDOList) {
                        //删除输出配置表
                        commonMapper.deleteDataById(destinationConfigDO.getDestinationType(), destinationConfigDO.getDestinationId());
                        //删除输出配置关联表
                        destinationConfigRelationMapper.deleteDataById(destinationConfigDO.getId());
                    }
                }

                //删除INFOSHARECONFIG
                infoShareConfigMapper.deleteDataById(infoshareId);
                Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE).warn("链路删除成功!");
            }
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("InfoshareServiceImpl类的removeLink方法出现错误，错误原因：", e);
            throw new Exception("删除链路失败！详情请查看日志！");
        }
    }

    /**
     * 更新输出配置
     *
     * @param infoshareConfigId 链路ID
     * @param data              配置信息
     */
    private void saveDestinations(Long infoshareConfigId, Map<String, Object> data) {
        //新增、修改activemq配置
        DestinationConfigDO destinationConfigDO;
        ArrayList<Map> activemqConfigList = (ArrayList<Map>) data.get(Constants.ACTIVEMQCONFIG);
        List<Long> activemqIdLists = new ArrayList<>();
        //修改前的输出目的地配置
        DestinationConfigDO oldDestinationParam = new DestinationConfigDO();
        oldDestinationParam.setInfoshareconfigId(infoshareConfigId);
        oldDestinationParam.setDestinationType(Constants.ACTIVEMQCONFIG);
        List<DestinationConfigDO> oldDestinationConfigDOList = destinationConfigRelationMapper.listData(oldDestinationParam);
        if (activemqConfigList != null && activemqConfigList.size() > 0) {
            for (Map o : activemqConfigList) {
                if (o.isEmpty()) {
                    continue;
                }
                ActiveMqConfigDO activeMqConfigDO = JSON.parseObject(JSON.toJSONString(o), ActiveMqConfigDO.class);
                destinationConfigDO = activeMqConfigDO.getDestinationConfigDO();
                if (activeMqConfigDO.getId() == null) {
                    activemqConfigMapper.addData(activeMqConfigDO);
                    destinationConfigDO.setInfoshareconfigId(infoshareConfigId);
                    PropertyDictDO propertyDictDO = propertyDictMapper.getPropertyDictByTypeNameAndDictName(PropertyNameConstants.DESTINATION_TYPE, PropertyNameConstants.ACTIVE_MQ);
                    destinationConfigDO.setDestinationType(propertyDictDO.getRelationTable());
                    destinationConfigDO.setDestinationTypeId(propertyDictDO.getId());
                    destinationConfigDO.setDestinationId(activeMqConfigDO.getId());
                    destinationConfigRelationMapper.addData(destinationConfigDO);
                } else {
                    activemqIdLists.add(activeMqConfigDO.getId());
                    activemqConfigMapper.updateById(activeMqConfigDO);
                    destinationConfigRelationMapper.updateById(destinationConfigDO);
                }
            }
        }
        //删除activemq配置
        deleteDestinations(Constants.ACTIVEMQCONFIG, activemqIdLists, oldDestinationConfigDOList);

        //新增、修改ibmmq配置
        ArrayList<Map> ibmmqConfigList = (ArrayList<Map>) data.get(Constants.IBMMQCONFIG);
        List<Long> ibmmqIdLists = new ArrayList<>();
        //修改前的输出目的地配置
        oldDestinationParam.setDestinationType(Constants.IBMMQCONFIG);
        oldDestinationConfigDOList = destinationConfigRelationMapper.listData(oldDestinationParam);
        if (ibmmqConfigList != null && ibmmqConfigList.size() > 0) {
            for (Map o : ibmmqConfigList) {
                if (o.isEmpty()) {
                    continue;
                }
                IbmMqConfigDO ibmMqConfigDO = JSON.parseObject(JSON.toJSONString(o), IbmMqConfigDO.class);
                destinationConfigDO = ibmMqConfigDO.getDestinationConfigDO();
                if (ibmMqConfigDO.getId() == null) {
                    ibmmqConfigMapper.addData(ibmMqConfigDO);
                    destinationConfigDO.setInfoshareconfigId(infoshareConfigId);
                    PropertyDictDO propertyDictDO = propertyDictMapper.getPropertyDictByTypeNameAndDictName(PropertyNameConstants.DESTINATION_TYPE, PropertyNameConstants.IBM_MQ);
                    destinationConfigDO.setDestinationType(propertyDictDO.getRelationTable());
                    destinationConfigDO.setDestinationTypeId(propertyDictDO.getId());
                    destinationConfigDO.setDestinationId(ibmMqConfigDO.getId());
                    destinationConfigRelationMapper.addData(destinationConfigDO);
                } else {
                    ibmmqIdLists.add(ibmMqConfigDO.getId());
                    ibmmqConfigMapper.updateById(ibmMqConfigDO);
                    destinationConfigRelationMapper.updateById(destinationConfigDO);
                }
            }
        }
        //删除ibmmq配置
        deleteDestinations(Constants.IBMMQCONFIG, ibmmqIdLists, oldDestinationConfigDOList);

        //新增、修改ftp配置
        ArrayList<Map> ftpConfigList = (ArrayList<Map>) data.get(Constants.FTPCONFIG);
        List<Long> ftpIdLists = new ArrayList<>();
        //修改前的输出目的地配置
        oldDestinationParam.setDestinationType(Constants.FTPCONFIG);
        oldDestinationConfigDOList = destinationConfigRelationMapper.listData(oldDestinationParam);
        if (ftpConfigList != null && ftpConfigList.size() > 0) {
            for (Map o : ftpConfigList) {
                if (o.isEmpty()) {
                    continue;
                }
                FtpConfigDO ftpConfigDO = JSON.parseObject(JSON.toJSONString(o), FtpConfigDO.class);
                destinationConfigDO = ftpConfigDO.getDestinationConfigDO();
                if (ftpConfigDO.getId() == null) {
                    ftpConfigMapper.addData(ftpConfigDO);
                    destinationConfigDO.setInfoshareconfigId(infoshareConfigId);
                    PropertyDictDO propertyDictDO = propertyDictMapper.getPropertyDictByTypeNameAndDictName(PropertyNameConstants.DESTINATION_TYPE, PropertyNameConstants.FTP);
                    destinationConfigDO.setDestinationType(propertyDictDO.getRelationTable());
                    destinationConfigDO.setDestinationTypeId(propertyDictDO.getId());
                    destinationConfigDO.setDestinationId(ftpConfigDO.getId());
                    destinationConfigRelationMapper.addData(destinationConfigDO);
                } else {
                    ftpIdLists.add(ftpConfigDO.getId());
                    ftpConfigMapper.updateById(ftpConfigDO);
                    destinationConfigRelationMapper.updateById(destinationConfigDO);
                }
            }
        }
        //删除ftp配置
        deleteDestinations(Constants.FTPCONFIG, ftpIdLists, oldDestinationConfigDOList);

        //新增、修改tcpip配置
        ArrayList<Map> tcpipConfigList = (ArrayList<Map>) data.get(Constants.TCPIPCONFIG);
        List<Long> tcpipLists = new ArrayList<>();
        //修改前的输出目的地配置
        oldDestinationParam.setDestinationType(Constants.TCPIPCONFIG);
        oldDestinationConfigDOList = destinationConfigRelationMapper.listData(oldDestinationParam);
        if (tcpipConfigList != null && tcpipConfigList.size() > 0) {
            for (Map o : tcpipConfigList) {
                if (o.isEmpty()) {
                    continue;
                }
                TcpipConfigDO tcpipConfigDO = JSON.parseObject(JSON.toJSONString(o), TcpipConfigDO.class);
                destinationConfigDO = tcpipConfigDO.getDestinationConfigDO();
                if (tcpipConfigDO.getId() == null) {
                    tcpipConfigMapper.addData(tcpipConfigDO);
                    destinationConfigDO.setInfoshareconfigId(infoshareConfigId);
                    PropertyDictDO propertyDictDO = propertyDictMapper.getPropertyDictByTypeNameAndDictName(PropertyNameConstants.DESTINATION_TYPE, PropertyNameConstants.TCPIP);
                    destinationConfigDO.setDestinationType(propertyDictDO.getRelationTable());
                    destinationConfigDO.setDestinationTypeId(propertyDictDO.getId());
                    destinationConfigDO.setDestinationId(tcpipConfigDO.getId());
                    destinationConfigRelationMapper.addData(destinationConfigDO);
                } else {
                    tcpipLists.add(tcpipConfigDO.getId());
                    tcpipConfigMapper.updateById(tcpipConfigDO);
                    destinationConfigRelationMapper.updateById(destinationConfigDO);
                }
            }
        }
        //删除tcpip配置
        deleteDestinations(Constants.TCPIPCONFIG, tcpipLists, oldDestinationConfigDOList);
    }

    /**
     * 输出配置表的删除
     *
     * @param destinationType            输出方式
     * @param idList                     输出方式idList
     * @param oldDestinationConfigDOList 修改前的输出目的地配置
     */
    private void deleteDestinations(String destinationType, List<Long> idList, List<DestinationConfigDO> oldDestinationConfigDOList) {
        if (oldDestinationConfigDOList != null && oldDestinationConfigDOList.size() > 0) {
            for (DestinationConfigDO configDO : oldDestinationConfigDOList) {
                if (!idList.contains(configDO.getDestinationId())) {
                    //删除输出配置表
                    commonMapper.deleteDataById(destinationType, configDO.getDestinationId());
                    //删除输出配置关联表
                    destinationConfigRelationMapper.deleteDataById(configDO.getId());
                }
            }
        }
    }
}