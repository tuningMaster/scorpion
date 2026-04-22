package com.scorpion.config.facade.convert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.scorpion.config.common.utils.DateUtils;
import com.scorpion.config.facade.model.*;
import com.scorpion.config.facade.rpc.ConfigItem;
import com.scorpion.config.facade.rpc.RpcStrategyConfigDTO;
import com.scorpion.config.facade.rpc.RpcStrategyDTO;
import com.scorpion.config.impl.dal.ConfigDO;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 三层模型之间的转换工具类，全部为静态方法。
 * <p>
 * 空值安全规范：入参 null 返回 null，入参空 List 返回空 List。
 */
@SuppressWarnings({"DuplicateCode"})
public class ConfigConverter {
    /**
     * DTO → DO，strategyConfigs List 序列化为 JSON String
     */
    public static ConfigDO dto2Do(ConfigDTO dto) {
        if (dto == null) {
            return null;
        }
        ConfigDO doObj = new ConfigDO();
        doObj.setCreateTime(dto.getCreateTime());
        doObj.setUpdateTime(dto.getUpdateTime());
        doObj.setAppName(dto.getAppName());
        doObj.setType(dto.getType());
        doObj.setCustomId(dto.getCustomId());
        doObj.setName(dto.getName());
        doObj.setStatus(dto.getStatus());
        doObj.setContent(dto.getContent());
        doObj.setStrategyConfigs(dto.getCustomId());
        doObj.setExtra(dto.getExtra());
        return doObj;
    }

    public static List<ConfigDO> dto2DO(List<ConfigDTO> configDTOList) {
        if (CollectionUtils.isEmpty(configDTOList)) {
            return Lists.newArrayList();
        }

        return configDTOList.stream().map(ConfigConverter::dto2Do).collect(Collectors.toList());
    }



    public static ConfigDTO do2DTO(ConfigDO configDO) {
        if (configDO == null) {
            return null;
        }

        ConfigDTO dto = new ConfigDTO();
        dto.setCreateTime(configDO.getCreateTime());
        dto.setUpdateTime(configDO.getUpdateTime());
        dto.setAppName(configDO.getAppName());
        dto.setType(configDO.getType());
        dto.setCustomId(configDO.getCustomId());
        dto.setName(configDO.getName());
        dto.setStatus(configDO.getStatus());
        dto.setContent(configDO.getContent());
        dto.setStrategyConfigs(
                JSON.parseObject(configDO.getStrategyConfigs(), new TypeReference<List<StrategyConfigDTO>>() {
                }));
        dto.setExtra(configDO.getExtra());
        return dto;
    }

    /**
     * DO → DTO，strategyConfigs JSON String 反序列化为 List
     */
    public static List<ConfigDTO> do2DTO(List<ConfigDO> doObj) {
        if (CollectionUtils.isEmpty(doObj)) {
            return Lists.newArrayList();
        }

        return doObj.stream().map(ConfigConverter::do2DTO).collect(Collectors.toList());
    }

    /**
     * DO → RPC，Date 格式化为 String
     */
    public static ConfigItem do2RpcModel(ConfigDO doObj) {
        if (doObj == null) {
            return null;
        }
        ConfigItem item = new ConfigItem();
        item.setId(doObj.getId());
        item.setCreateTime(DateUtils.formatDate(doObj.getCreateTime()));
        item.setUpdateTime(DateUtils.formatDate(doObj.getUpdateTime()));
        item.setAppName(doObj.getAppName());
        item.setType(doObj.getType());
        item.setCustomId(doObj.getCustomId());
        item.setName(doObj.getName());
        item.setStatus(doObj.getStatus());
        item.setContent(doObj.getContent());
        item.setStrategyConfigs(doObj.getStrategyConfigs());
        item.setExtra(doObj.getExtra());
        return item;
    }

    /**
     * RPC → DO，String 解析为 Date
     */
    public static ConfigDO rpcModel2Do(ConfigItem item) {
        if (item == null) {
            return null;
        }
        ConfigDO doObj = new ConfigDO();
        doObj.setId(item.getId());
        doObj.setCreateTime(DateUtils.parseDate(item.getCreateTime()));
        doObj.setUpdateTime(DateUtils.parseDate(item.getUpdateTime()));
        doObj.setAppName(item.getAppName());
        doObj.setType(item.getType());
        doObj.setCustomId(item.getCustomId());
        doObj.setName(item.getName());
        doObj.setStatus(item.getStatus());
        doObj.setContent(item.getContent());
        doObj.setStrategyConfigs(item.getStrategyConfigs());
        doObj.setExtra(item.getExtra());
        return doObj;
    }

    public static List<ConfigItem> do2RpcModel(List<ConfigDO> dos) {
        if (dos == null || dos.isEmpty()) {
            return new ArrayList<>();
        }
        return dos.stream().map(ConfigConverter::do2RpcModel).collect(java.util.stream.Collectors.toList());
    }

    public static List<ConfigDO> rpcModel2Do(List<ConfigItem> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        return items.stream().map(ConfigConverter::rpcModel2Do).collect(java.util.stream.Collectors.toList());
    }

    /**
     * DTO → RPC，strategyConfigs List 序列化 + Date 格式化
     */
    public static ConfigItem dto2RpcModel(ConfigDTO dto) {
        if (dto == null) {
            return null;
        }
        ConfigItem item = new ConfigItem();
        item.setId(dto.getId());
        item.setCreateTime(DateUtils.formatDate(dto.getCreateTime()));
        item.setUpdateTime(DateUtils.formatDate(dto.getUpdateTime()));
        item.setAppName(dto.getAppName());
        item.setType(dto.getType());
        item.setCustomId(dto.getCustomId());
        item.setName(dto.getName());
        item.setStatus(dto.getStatus());
        item.setContent(dto.getContent());
        item.setStrategyConfigs(JSON.toJSONString(dto.getStrategyConfigs()));
        item.setExtra(dto.getExtra());
        return item;
    }

    /**
     * RPC → DTO，strategyConfigs 反序列化 + Date 解析
     */
    public static ConfigDTO rpcModel2Dto(ConfigItem item) {
        if (item == null) {
            return null;
        }
        ConfigDTO dto = new ConfigDTO();
        dto.setId(item.getId());
        dto.setCreateTime(DateUtils.parseDate(item.getCreateTime()));
        dto.setUpdateTime(DateUtils.parseDate(item.getUpdateTime()));
        dto.setAppName(item.getAppName());
        dto.setType(item.getType());
        dto.setCustomId(item.getCustomId());
        dto.setName(item.getName());
        dto.setStatus(item.getStatus());
        dto.setContent(item.getContent());
        dto.setStrategyConfigs(JSON.parseObject(item.getStrategyConfigs(), new TypeReference<List<StrategyConfigDTO>>() {}));
        dto.setExtra(item.getExtra());
        return dto;
    }

    public static List<ConfigItem> dto2RpcModel(List<ConfigDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return new ArrayList<>();
        }
        return dtos.stream().map(ConfigConverter::dto2RpcModel).collect(java.util.stream.Collectors.toList());
    }

    public static List<ConfigDTO> rpcModel2Dto(List<ConfigItem> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        return items.stream().map(ConfigConverter::rpcModel2Dto).collect(java.util.stream.Collectors.toList());
    }
}
