package com.scorpion.core.processor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Sets;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Set;

/**
 * 文件读写
 */
public class ResourceProcessor {
    /**
     * 文件写入
     *
     * @param filer        文件维护
     * @param resourcePath 写入路径
     * @param resourceSet  写入内容
     * @param <T>          类型
     * @throws IOException 异常
     */
    public static <T> void write(Filer filer, String resourcePath, Set<T> resourceSet) throws IOException {
        FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourcePath);

        try (OutputStream out = fileObject.openOutputStream()) {
            String config = JSON.toJSONString(resourceSet, SerializerFeature.PrettyFormat);
            out.write(config.getBytes(StandardCharsets.UTF_8));
        }
    }


    public static <T> Set<T> load(ClassLoader classLoader, String resourceName, TypeReference<Set<T>> typeReference) {
        Set<T> results = Sets.newHashSet();
        try {
            Enumeration<URL> enumeration = classLoader.getResources(resourceName);
            while (enumeration.hasMoreElements()) {
                URL url = enumeration.nextElement();
                InputStream inputStream = url.openStream();
                Set<T> resourceSet = JSON.parseObject(inputStream, StandardCharsets.UTF_8, typeReference.getType());
                results.addAll(resourceSet);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return results;
    }
}
