package com.scorpion.core.processor;

import com.scorpion.core.annotation.scan.CompileMetaScan;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

public class CompileScanMetaAnnotationProcessor extends AbstractResourceProcessor<String> {
    @Override
    protected Set<Class<? extends Annotation>> annotationTypes() {
        return Collections.singleton(CompileMetaScan.class);
    }

    @Override
    public String resourcePath() {
        return ResourceConstants.COMPILE_SCAN_ANNOTATIONS_FILE;
    }

    @Override
    protected Set<String> processElement(Element element, RoundEnvironment roundEnv, ProcessingEnvironment processingEnv) {
        if (element.getKind() == ElementKind.ANNOTATION_TYPE) {
            String binaryName = processingEnv.getElementUtils().getBinaryName((TypeElement) element).toString();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "[CompileScanMeta] add to config:"
                    + binaryName, element);
            return Collections.singleton(binaryName);
        }

        return Collections.emptySet();
    }
}
