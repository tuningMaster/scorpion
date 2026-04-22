package com.scorpion.config.impl.dal;

import java.util.List;

/**
 * 配置表 DAO 接口
 */
public interface ConfigRepository {
    /**
     * 新增一条配置
     */
    int insert(ConfigDO configDo);

    /**
     * 根据unique key修改一个配置
     */
    int updateByUK(ConfigDO configDO);

    /**
     * 根据uk完全删除一条数据
     */
    int deleteByUK(String appName, String type, String customId);

    /**
     * 根据UK查询一个配置
     */
    ConfigDO selectByUK(String appName, String type, String customId);


    /**
     * 查询所有 ONLINE 状态的配置
     */
    List<ConfigDO> listAllOnlineConfig();

    /**
     * 分页查询
     */
    List<ConfigDO> listByCondition(String appName, String type, List<String> status, String searchWord, int pageSize, int pageNum);

    /**
     * 根据appName & configType查询总数
     */
    int countByCondition(String appName, String type, List<String> status, String searchWord);
}
