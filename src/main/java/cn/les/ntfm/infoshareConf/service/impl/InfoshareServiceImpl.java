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
            //??????infoshareconfig
            InfoshareConfigDO infoshareConfigDO = JSON.parseObject(JSON.toJSONString(data.get("infoshareConfig")), InfoshareConfigDO.class);
            infoShareConfigMapper.addData(infoshareConfigDO);

            //??????????????????
            saveDestinations(infoshareConfigDO.getId(), data);
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("EditLinkServiceImpl??????saveEditMsgs????????????????????????????????????", e);
            throw new Exception("???????????????????????????????????????");
        }
    }

    @Override
    public void updateEditMsgs(JSONObject data) throws Exception {
        InfoshareConfigDO infoshareConfigDO = JSON.parseObject(JSON.toJSONString(data.get("infoshareConfig")), InfoshareConfigDO.class);
        //???????????????
        InfoshareConfigDO oldInfoshareConfig = infoShareConfigMapper.getDataById(infoshareConfigDO.getId());
        if (oldInfoshareConfig == null
                || !infoshareConfigDO.getUpdateTime().equals(oldInfoshareConfig.getUpdateTime())) {
            throw new Exception("????????????????????????????????????");
        }

        try {
            //??????infoshareconfig
            infoShareConfigMapper.updateDataById(infoshareConfigDO);
            infoShareConfigMapper.getDataById(infoshareConfigDO.getId());
            //??????????????????
            saveDestinations(infoshareConfigDO.getId(), data);
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("EditLinkServiceImpl??????updateEditMsgs????????????????????????????????????", e);
            throw new Exception("???????????????????????????????????????");
        }
    }

    @Override
    public Map<String, Object> getMsgsById(long linkId) {
        Map<String, Object> result = new HashMap<>(Constants.HASHMAP_INITIAL_CAPACITY);
        //infoshareconfig
        InfoshareConfigDO infoshareConfigDO = infoShareConfigMapper.getDataById(linkId);
        result.put("infoshareConfig", infoshareConfigDO);
        //???????????????
        DestinationConfigDO destinationParam = new DestinationConfigDO();
        destinationParam.setInfoshareconfigId(linkId);

        //???????????????activemq
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

        //???????????????ibmmq
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

        //???????????????ftp
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

        //???????????????tcpip
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
                //???????????????????????????
                Long sourceIdNew = null;
                if (infoshareConfigOld.getSourceId() != null) {
                    DataBaseConfigDO dataBaseConfigOld = databaseConfigMapper.getDataById(infoshareConfigOld.getSourceId());
                    if (dataBaseConfigOld != null) {
                        //??????XML???????????????????????????
                        Long xmlformatId = recursiveXmlFormatAndOutputRelation(null, xmlFormatConfigMapper.getDataById(dataBaseConfigOld.getXmlformatconfigId()));
                        //?????????????????????
                        DataBaseConfigDO dataBaseConfigDO = new DataBaseConfigDO();
                        BeanUtils.copyProperties(dataBaseConfigOld, dataBaseConfigDO);
                        dataBaseConfigDO.setXmlformatconfigId(xmlformatId);
                        dataBaseConfigDO.setId(null);
                        dataBaseConfigDO.setUpdateTime(null);
                        dataBaseConfigDO.setUpdateUser(null);
                        databaseConfigMapper.addData(dataBaseConfigDO);
                        sourceIdNew = dataBaseConfigDO.getId();
                        //???????????????????????????
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

                //??????????????????????????????
                InfoshareConfigDO infoshareConfigDO = new InfoshareConfigDO();
                BeanUtils.copyProperties(infoshareConfigOld, infoshareConfigDO);
                infoshareConfigDO.setSourceId(sourceIdNew);
                infoshareConfigDO.setStateFlag(false);
                infoshareConfigDO.setId(null);
                infoshareConfigDO.setUpdateTime(null);
                infoshareConfigDO.setUpdateUser(null);
                infoShareConfigMapper.addData(infoshareConfigDO);

                //???????????????????????????
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
                    .error("InfoshareServiceImpl??????updateCopyLinks????????????????????????????????????", e);
            throw new Exception("?????????????????????????????????????????????");
        }
    }

    private Long recursiveXmlFormatAndOutputRelation(Long pid, XmlFormatConfigDO xmlFormatConfigOld) {
        XmlFormatConfigDO xmlFormatConfigDO = new XmlFormatConfigDO();
        BeanUtils.copyProperties(xmlFormatConfigOld, xmlFormatConfigDO);
        //????????????????????????
        if (xmlFormatConfigOld.getOutputrelationId() != null) {
            OutputRelationDO outputRelationOld = outputRelationMapper.getDataById(xmlFormatConfigOld.getOutputrelationId());
            outputRelationOld.setId(null);
            outputRelationOld.setCreateTime(null);
            outputRelationOld.setCreateUser(null);
            outputRelationMapper.addData(outputRelationOld);

            xmlFormatConfigDO.setOutputrelationId(outputRelationOld.getId());
        }
        //??????XML??????
        xmlFormatConfigDO.setPid(pid);
        xmlFormatConfigDO.setId(null);
        xmlFormatConfigDO.setUpdateTime(null);
        xmlFormatConfigDO.setUpdateUser(null);
        xmlFormatConfigMapper.addData(xmlFormatConfigDO);

        //????????????XML?????????????????????
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
                //???????????????
                if (infoshareConfigDelete.getSourceId() != null) {
                    //??????XMLFORMATCONFIG???OUTPUTRELATION
                    DataBaseConfigDO dataBaseConfigDelete = databaseConfigMapper.getDataById(infoshareConfigDelete.getSourceId());
                    if (dataBaseConfigDelete != null) {
                        if (dataBaseConfigDelete.getXmlformatconfigId() != null) {
                            XmlFormatConfigDO xmlFormatConfigDelete = new XmlFormatConfigDO();
                            xmlFormatConfigDelete.setId(dataBaseConfigDelete.getXmlformatconfigId());
                            dataSourceConfigService.removeChildrenXmlFormat(xmlFormatConfigDelete);
                        }
                        //??????DATABASRELATION
                        databaseRelationMapper.deleteDataByDatabaseconfigId(dataBaseConfigDelete.getId());
                        //??????DATABASECONFIG
                        databaseConfigMapper.deleteDataById(dataBaseConfigDelete.getId());
                    }
                }

                //???????????????????????????????????????
                DestinationConfigDO destinationParam = new DestinationConfigDO();
                destinationParam.setInfoshareconfigId(infoshareId);
                List<DestinationConfigDO> destinationConfigDOList = destinationConfigRelationMapper.listData(destinationParam);
                if (destinationConfigDOList != null && destinationConfigDOList.size() > 0) {
                    for (DestinationConfigDO destinationConfigDO : destinationConfigDOList) {
                        //?????????????????????
                        commonMapper.deleteDataById(destinationConfigDO.getDestinationType(), destinationConfigDO.getDestinationId());
                        //???????????????????????????
                        destinationConfigRelationMapper.deleteDataById(destinationConfigDO.getId());
                    }
                }

                //??????INFOSHARECONFIG
                infoShareConfigMapper.deleteDataById(infoshareId);
                Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE).warn("??????????????????!");
            }
        } catch (Exception e) {
            Log4jUtils.getInstance().getLogger(LogConstants.INFOSHARE)
                    .error("InfoshareServiceImpl??????removeLink????????????????????????????????????", e);
            throw new Exception("?????????????????????????????????????????????");
        }
    }

    /**
     * ??????????????????
     *
     * @param infoshareConfigId ??????ID
     * @param data              ????????????
     */
    private void saveDestinations(Long infoshareConfigId, Map<String, Object> data) {
        //???????????????activemq??????
        DestinationConfigDO destinationConfigDO;
        ArrayList<Map> activemqConfigList = (ArrayList<Map>) data.get(Constants.ACTIVEMQCONFIG);
        List<Long> activemqIdLists = new ArrayList<>();
        //?????????????????????????????????
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
        //??????activemq??????
        deleteDestinations(Constants.ACTIVEMQCONFIG, activemqIdLists, oldDestinationConfigDOList);

        //???????????????ibmmq??????
        ArrayList<Map> ibmmqConfigList = (ArrayList<Map>) data.get(Constants.IBMMQCONFIG);
        List<Long> ibmmqIdLists = new ArrayList<>();
        //?????????????????????????????????
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
        //??????ibmmq??????
        deleteDestinations(Constants.IBMMQCONFIG, ibmmqIdLists, oldDestinationConfigDOList);

        //???????????????ftp??????
        ArrayList<Map> ftpConfigList = (ArrayList<Map>) data.get(Constants.FTPCONFIG);
        List<Long> ftpIdLists = new ArrayList<>();
        //?????????????????????????????????
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
        //??????ftp??????
        deleteDestinations(Constants.FTPCONFIG, ftpIdLists, oldDestinationConfigDOList);

        //???????????????tcpip??????
        ArrayList<Map> tcpipConfigList = (ArrayList<Map>) data.get(Constants.TCPIPCONFIG);
        List<Long> tcpipLists = new ArrayList<>();
        //?????????????????????????????????
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
        //??????tcpip??????
        deleteDestinations(Constants.TCPIPCONFIG, tcpipLists, oldDestinationConfigDOList);
    }

    /**
     * ????????????????????????
     *
     * @param destinationType            ????????????
     * @param idList                     ????????????idList
     * @param oldDestinationConfigDOList ?????????????????????????????????
     */
    private void deleteDestinations(String destinationType, List<Long> idList, List<DestinationConfigDO> oldDestinationConfigDOList) {
        if (oldDestinationConfigDOList != null && oldDestinationConfigDOList.size() > 0) {
            for (DestinationConfigDO configDO : oldDestinationConfigDOList) {
                if (!idList.contains(configDO.getDestinationId())) {
                    //?????????????????????
                    commonMapper.deleteDataById(destinationType, configDO.getDestinationId());
                    //???????????????????????????
                    destinationConfigRelationMapper.deleteDataById(configDO.getId());
                }
            }
        }
    }
}