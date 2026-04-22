package com.scorpion.config.facade.model;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 全量配置包装对象，ConfigProvider接口的返回值
 * <p>version >= 0，configList 不为空</p>
 */
@Data
public class AppAllConfigDTO implements Serializable {

    @Min(0)
    private long version;

    @NotNull
    private List<ConfigDTO> configList;
}
