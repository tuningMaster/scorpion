package com.scorpion.core.annotation.scan;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  编译器扫描，元注解
 *  <p>
 *      CompileMetaScan打到一个用户的注解上，用户的注解再打到具体的元素上。
 *      元素会被scanner处理，入参是打了注解的注解对应的Element(类，字段，方法等)
 *      scanner 返回的结果，会被合并成一个set(去重复)以后，写入到resourceFile中。
 *  </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface CompileMetaScan {

    /**
     * 资源文件,必须是META-INF目录下的
     * <p>
     *     例如META-INF/scorpion/interfaces.json
     *     可以把多个scanner的结果放到相同的资源文件下，前提是结果类型是一样的
     * </p>
     */
    String resourceFile();

    /**
     * scanner,需要继承CompileScanner, 有无参构造
     * <p>
     *     1. 这里写类名而不是类是因为processor处理有可能有顺序问题（可能类还不存在）
     *     2. 如果和别的scan的resourceFile一致，那么会合并到一起
     * </p>
     *
     */
    String scanner() default "com.scorpion.core.processor.TypeStringScanner";
}
