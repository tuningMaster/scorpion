package com.scorpion.core.annotation.scan;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import java.util.Set;

public interface CompileScanner<T> {
    Set<T> scan(Element element, RoundEnvironment roundEnv, ProcessingEnvironment processingEnv);
}
