package com.scorpion.core.test.core.scan;

import com.scorpion.core.annotation.scan.CompileScanMeta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@CompileScanMeta(resourceFile = ScanConstants.RESOURCE_FILE_1)
public @interface TestScan {
}
