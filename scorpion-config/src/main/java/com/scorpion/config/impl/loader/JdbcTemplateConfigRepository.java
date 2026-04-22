package com.scorpion.config.impl.loader;

import com.scorpion.config.impl.dal.ConfigDO;
import com.scorpion.config.impl.dal.ConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 基于 NamedParameterJdbcTemplate 的配置仓库实现
 */
public class JdbcTemplateConfigRepository implements ConfigRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplateConfigRepository.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final String tableName;

    private static final RowMapper<ConfigDO> ROW_MAPPER = new RowMapper<ConfigDO>() {
        @Override
        public ConfigDO mapRow(ResultSet rs, int rowNum) throws SQLException {
            ConfigDO configDO = new ConfigDO();
            configDO.setId(rs.getLong("id"));
            configDO.setAppName(rs.getString("app_name"));
            configDO.setType(rs.getString("type"));
            configDO.setCustomId(rs.getString("custom_id"));
            configDO.setName(rs.getString("name"));
            configDO.setStatus(rs.getString("status"));
            configDO.setContent(rs.getString("content"));
            configDO.setStrategyConfigs(rs.getString("strategy_configs"));
            configDO.setExtra(rs.getString("extra"));
            configDO.setCreateTime(rs.getTimestamp("create_time"));
            configDO.setUpdateTime(rs.getTimestamp("update_time"));
            return configDO;
        }
    };

    public JdbcTemplateConfigRepository(NamedParameterJdbcTemplate jdbcTemplate,
                                        String tableName) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
    }

    @Override
    public List<ConfigDO> listAllOnlineConfig() {
        String sql = "SELECT * FROM " + tableName + " WHERE status = 'ONLINE'";
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    @Override
    public int insert(ConfigDO configDo) {
        String sql = "INSERT INTO " + tableName
                + " (app_name, type, custom_id, name, status, content, strategy_configs, extra, create_time, update_time) "
                + "VALUES (:appName, :type, :customId, :name, :status, :content, :strategyConfigs, :extra, :createTime, :updateTime)";
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("appName", configDo.getAppName())
                .addValue("type", configDo.getType())
                .addValue("customId", configDo.getCustomId())
                .addValue("name", configDo.getName())
                .addValue("status", configDo.getStatus())
                .addValue("content", configDo.getContent())
                .addValue("strategyConfigs", configDo.getStrategyConfigs())
                .addValue("extra", configDo.getExtra())
                .addValue("createTime", configDo.getCreateTime())
                .addValue("updateTime", configDo.getUpdateTime()));
    }

    @Override
    public int updateByUK(ConfigDO configDO) {
        String sql = "UPDATE " + tableName
                + " SET name = :name, status = :status, content = :content, "
                + "strategy_configs = :strategyConfigs, extra = :extra, update_time = :updateTime "
                + "WHERE app_name = :appName AND type = :type AND custom_id = :customId";
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("name", configDO.getName())
                .addValue("status", configDO.getStatus())
                .addValue("content", configDO.getContent())
                .addValue("strategyConfigs", configDO.getStrategyConfigs())
                .addValue("extra", configDO.getExtra())
                .addValue("updateTime", configDO.getUpdateTime())
                .addValue("appName", configDO.getAppName())
                .addValue("type", configDO.getType())
                .addValue("customId", configDO.getCustomId()));
    }

    @Override
    public int deleteByUK(String appName, String type, String customId) {
        String sql = "DELETE FROM " + tableName
                + " WHERE app_name = :appName AND type = :type AND custom_id = :customId";
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("appName", appName)
                .addValue("type", type)
                .addValue("customId", customId));
    }

    @Override
    public ConfigDO selectByUK(String appName, String type, String customId) {
        String sql = "SELECT * FROM " + tableName
                + " WHERE app_name = :appName AND type = :type AND custom_id = :customId LIMIT 1";
        List<ConfigDO> result = jdbcTemplate.query(sql, new MapSqlParameterSource()
                .addValue("appName", appName)
                .addValue("type", type)
                .addValue("customId", customId), ROW_MAPPER);
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<ConfigDO> listByCondition(String appName, String type, List<String> status, String searchWord, int pageSize, int pageNum) {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName + " WHERE 1=1");
        MapSqlParameterSource params = new MapSqlParameterSource();
        appendCondition(sql, params, appName, type, status, searchWord);
        sql.append(" ORDER BY id DESC LIMIT :limit OFFSET :offset");
        params.addValue("limit", pageSize);
        params.addValue("offset", (pageNum - 1) * pageSize);
        return jdbcTemplate.query(sql.toString(), params, ROW_MAPPER);
    }

    @Override
    public int countByCondition(String appName, String type, List<String> status, String searchWord) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + tableName + " WHERE 1=1");
        MapSqlParameterSource params = new MapSqlParameterSource();
        appendCondition(sql, params, appName, type, status, searchWord);
        return jdbcTemplate.queryForObject(sql.toString(), params, Integer.class);
    }

    private void appendCondition(StringBuilder sql, MapSqlParameterSource params,
                                 String appName, String type, List<String> status, String searchWord) {
        sql.append(" AND app_name = :appName");
        params.addValue("appName", appName);
        if (type != null && !type.isEmpty()) {
            sql.append(" AND type = :type");
            params.addValue("type", type);
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status IN (:status)");
            params.addValue("status", status);
        }
        if (searchWord != null && !searchWord.isEmpty()) {
            sql.append(" AND (name LIKE :searchWord OR custom_id LIKE :searchWord)");
            params.addValue("searchWord", "%" + searchWord + "%");
        }
    }
}
