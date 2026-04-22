package com.scorpion.core.processor;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 抽取公共逻辑
 */
public abstract class AbstractResourceProcessor<T> extends AbstractProcessor {
    private final Set<T> founds = Sets.newConcurrentHashSet();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            find(roundEnv);
        } else {
            generateFiles();
        }
        return false;
    }

    protected void find(RoundEnvironment roundEnv) {
        for (Class<? extends Annotation> annotationType : annotationTypes()) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotationType);
            for (Element element : elements) {
                founds.addAll(processElement(element, roundEnv, processingEnv));
            }
        }
    }

    private void generateFiles() {
        if (founds.isEmpty()) {
            return;
        }

        Filer filer = processingEnv.getFiler();
        try {
            ResourceProcessor.write(filer, resourcePath(), founds);
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "io exception " + ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return annotationTypes().stream().map(Class::getCanonicalName).collect(Collectors.toSet());
    }

    protected abstract Set<Class<? extends Annotation>> annotationTypes();


    /**
     * 资源路径
     */
    public abstract String resourcePath();

    /**
     * 处理找到的Element
     */
    protected abstract Set<T> processElement(Element element, RoundEnvironment roundEnv,
                                             ProcessingEnvironment processingEnv);
}
