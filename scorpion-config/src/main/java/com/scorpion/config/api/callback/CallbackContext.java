package com.scorpion.config.api.callback;

import com.scorpion.config.facade.enums.ConfigChangeTypeEnum;
import com.scorpion.config.facade.enums.OperateTypeEnum;
import com.scorpion.config.facade.model.ConfigDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置变更回调上下文
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallbackContext {
    /**
     * 配置变更类型
     */
    private ConfigChangeTypeEnum changeType;

    /**
     * 新建配置/修改配置
     */
    private OperateTypeEnum operateType;
}
