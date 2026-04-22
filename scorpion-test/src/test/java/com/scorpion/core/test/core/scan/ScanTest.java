package com.scorpion.core.test.core.scan;

import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.scorpion.core.processor.ResourceProcessor;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ScanTest {

    @Test
    public void test() {
        Set<String> strings = ResourceProcessor.load(ScanTest.class.getClassLoader(), ScanConstants.RESOURCE_FILE_1,
                new TypeReference<Set<String>>() {});
        List<String> lists = strings.stream().sorted().collect(Collectors.toList());
        assertEquals(Lists.newArrayList("com.scorpion.core.test.core.scan.clazz.TestScanClass"), lists);
    }
}
