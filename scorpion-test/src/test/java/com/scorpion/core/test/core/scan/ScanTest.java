package com.scorpion.core.test.core.scan;

import com.alibaba.fastjson.TypeReference;
import com.scorpion.core.processor.ResourceProcessor;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ScanTest {

    @Test
    public void test() {
        Set<String> strings = ResourceProcessor.load(ScanTest.class.getClassLoader(), ScanConstants.RESOURCE_FILE_1,
                new TypeReference<Set<String>>() {});
        List<String> lists = strings.stream().sorted().collect(Collectors.toList());
        System.out.println(lists);
    }
}
