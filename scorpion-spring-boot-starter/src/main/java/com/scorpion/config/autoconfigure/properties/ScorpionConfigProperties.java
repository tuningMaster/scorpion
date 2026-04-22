package com.scorpion.config.autoconfigure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * scorpion-config 自动配置属性
 * scorpion.config.enabled=false 时不初始化
 * scorpion.config.app-name 和 scorpion.config.table-name 为必填
 */
@ConfigurationProperties(prefix = "scorpion.config")
public class ScorpionConfigProperties {

    private boolean enabled = true;
    private String appName;
    private String tableName;
    private long reloadInterval = 30000;
    private int threadSize = 5;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public long getReloadInterval() {
        return reloadInterval;
    }

    public void setReloadInterval(long reloadInterval) {
        this.reloadInterval = reloadInterval;
    }

    public int getThreadSize() {
        return threadSize;
    }

    public void setThreadSize(int threadSize) {
        this.threadSize = threadSize;
    }
}
