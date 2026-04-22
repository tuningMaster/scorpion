package com.scorpion.common.rpc;

import com.scorpion.common.exception.AssertUtils;
import com.scorpion.common.exception.CommonException;
import com.scorpion.common.exception.CommonResultCodeEnum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * Facade 模板方法，非 AOP 方式统一处理参数校验、异常捕获、Result 填充
 */
@Slf4j
public class FacadeTemplate {

    /**
     * 执行 RPC 调用，返回完整异常信息（后端服务间调用）
     */
    public static <REQ, RES> RES execute(REQ request, FacadeCallback<REQ, RES> callback) {
        return doExecute(request, callback, true);
    }

    /**
     * 执行 RPC 调用，隐藏堆栈信息（面向前端的接口）
     */
    public static <REQ, RES> RES executeForFE(REQ request, FacadeCallback<REQ, RES> callback) {
        return doExecute(request, callback, false);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private static <REQ, RES> RES doExecute(REQ request, FacadeCallback<REQ, RES> callback, boolean returnExStack) {
        Type[] genericTypes = ((ParameterizedType) (callback.getClass().getGenericInterfaces()[0])).getActualTypeArguments();

        Class<RES> responseType = (Class<RES>) genericTypes[1];
        String identity = callback.identifier();

        try {
            callback.checkParameters(request);

            RES response = callback.execute(request);
            fillResult(response, successs());

            return response;
        } catch (Throwable e) {
            RES response = responseType.newInstance();
            fillResult(response, fail(e, returnExStack));
            return response;
        }
    }

    @SneakyThrows
    private static <RES> void fillResult(RES response, Result result) {
        AssertUtils.isNotNull(response, CommonResultCodeEnum.SYSTEM_ERROR, "方法返回值不为空");
        AssertUtils.isNotNull(result, CommonResultCodeEnum.SYSTEM_ERROR, "Result 不能为空");
        Field resultField = Arrays.stream(response.getClass().getDeclaredFields())
                .filter(field -> Result.class.isAssignableFrom(field.getType()))
                .findAny()
                .orElse(null);
        AssertUtils.isNotNull(resultField, CommonResultCodeEnum.SYSTEM_ERROR,
                "返回值中不包含Result类型的字段");
        resultField.setAccessible(true);
        resultField.set(response, result);
    }

    private static Result successs() {
        Result result = new Result();
        result.setSuccess(true);
        result.setMessage("OK");
        result.setCode(0);
        return result;
    }


    private static Result fail(Throwable e, boolean returnExStack) {
        Result result = new Result();
        result.setSuccess(false);
        if (e instanceof CommonException) {
            result.setCode((((CommonException) e).getCode()));
            result.setMessage(e.getMessage());
        } else {
            if (returnExStack) {

            } else {
                result.setCode(CommonResultCodeEnum.SYSTEM_ERROR.getCode());
                result.setMessage(CommonResultCodeEnum.SYSTEM_ERROR.getMessage());
            }
        }

        return result;
    }
}
