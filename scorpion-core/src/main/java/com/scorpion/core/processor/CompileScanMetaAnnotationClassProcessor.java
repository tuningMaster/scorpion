package com.scorpion.core.processor;

import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.scorpion.core.annotation.scan.CompileScanMeta;
import com.scorpion.core.annotation.scan.CompileScanner;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 处理打了{@link CompileScanMeta}用户注解的class
 */
public class CompileScanMetaAnnotationClassProcessor extends AbstractProcessor {
    // resource 文件格式
    private static final Pattern RESOURCE_FILE_PATTERN = Pattern.compile("META-INF/.+/.+\\.json");

    /**
     * 扫描到的元素
     * k -> resourceFile文件相对路径
     * v -> 该文件的元素
     */
    private final Map<String, Set<Object>> founds = Maps.newConcurrentMap();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            if (!roundEnv.processingOver()) {
                find(roundEnv);
            } else {
                generateResourceFile();
            }
        } catch (RuntimeException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "[CompileScan] error" + ExceptionUtils.getStackTrace(e));
        }
        return false;
    }

    private void find(RoundEnvironment roundEnv) {
        for (Class<? extends Annotation> annotationType : annotationTypes()) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotationType);
            if (annotationType == CompileScanMeta.class) {
                // 本包内打了CompileScanMeta的用户注解
                for (Element element : elements) {
                    if (element.getKind() != ElementKind.ANNOTATION_TYPE) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                "element not annotation type", element);
                        continue;
                    }

                    CompileScanMeta metaScan = element.getAnnotation(CompileScanMeta.class);
                    if (metaScan == null) {
                        continue;
                    }

                    for (Element annotated : roundEnv.getElementsAnnotatedWith((TypeElement) element)) {
                        scanAndAdd(metaScan.resourceFile(), roundEnv, annotated, metaScan.scanner());
                    }
                }
            } else {
                // 其他包打了CompileScanMeta的注解被打到类本包的元素上
                for (Element element : elements) {
                    Annotation annotation = element.getAnnotation(annotationType);
                    CompileScanMeta metaScan = annotation.annotationType().getAnnotation(CompileScanMeta.class);
                    if (metaScan == null) {
                        continue;
                    }

                    scanAndAdd(metaScan.resourceFile(), roundEnv, element, metaScan.scanner());
                }
            }
        }
    }

    private void generateResourceFile() {
        if (founds.isEmpty()) {
            return;
        }

        // 资源文件
        Filer filer = processingEnv.getFiler();

        for (Map.Entry<String, Set<Object>> entry : founds.entrySet()) {
            try {
                ResourceProcessor.write(filer, entry.getKey(), entry.getValue());
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "io exception" + ExceptionUtils.getStackTrace(e));
            }
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return annotationTypes().stream().map(Class::getCanonicalName).collect(Collectors.toSet());
    }

    /**
     * 关心的注解类
     *
     * @return 注解类的集合
     */
    @SuppressWarnings("unchecked")
    private Set<Class<? extends Annotation>> annotationTypes() {
        Set<Class<? extends Annotation>> set = Sets.newHashSet();
        // 本包内打了CompileScanMeta的注解用户注解
        set.add(CompileScanMeta.class);

        // 其他包打了CompileScanMeta的注解被打到了本包的元素上
        for (String annotationClass : ResourceProcessor.load(CompileScanMeta.class.getClassLoader(),
                ResourceConstants.COMPILE_SCAN_ANNOTATIONS_FILE, new TypeReference<Set<String>>() {
                })) {
            try {
                set.add((Class<? extends Annotation>) Class.forName(annotationClass));
            } catch (ClassNotFoundException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING,
                        "[CompileScan]class not fount:" + annotationClass);
            }
        }

        return set;
    }

    private void scanAndAdd(String resourceFile, RoundEnvironment roundEnv, Element element, String scannerName) {
        // 格式检查
        if (!RESOURCE_FILE_PATTERN.matcher(resourceFile).matches()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "resourceFile:" + resourceFile + " not match format:" + RESOURCE_FILE_PATTERN.pattern(),
                    element);
            return;
        }

        founds.computeIfAbsent(resourceFile, any -> Sets.newConcurrentHashSet())
                .addAll(scan(roundEnv, element, scannerName));
    }

    /**
     * 扫描
     * @param roundEnv 环境
     * @param element 元素
     * @param scannerName scanner全限定类名
     * @return 文件内容
     */
    @SuppressWarnings("unchecked")
    private Set<Object> scan(RoundEnvironment roundEnv, Element element, String scannerName) {
        CompileScanner<Object> scanner;
        try {
            scanner = (CompileScanner<Object>) Class.forName(scannerName).newInstance();
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "scanner not found:" + scannerName, element);

            return Collections.emptySet();
        }

        Set<Object> result = scanner.scan(element, roundEnv, processingEnv);
        if (result == null) {
            return Collections.emptySet();
        }

        return result;
    }
}
