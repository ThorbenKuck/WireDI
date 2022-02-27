package com.github.thorbenkuck.di.processor;

import com.github.thorbenkuck.di.processor.foundation.ProcessorContext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AspectIgnoredAnnotations {

    private final List<String> classNames;

    public AspectIgnoredAnnotations(List<String> classNames) {
        this.classNames = classNames;
    }

    private static String FILE_NAME = "aop-ignored.types";

    public static AspectIgnoredAnnotations get() {
        try {
            FileObject resource = ProcessorContext.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", FILE_NAME);
            BufferedReader reader = new BufferedReader(resource.openReader(true));
            List<String> classNames = new ArrayList<>();

            while (reader.ready()) {
                String line = reader.readLine();
                classNames.add(line);
            }

            return new AspectIgnoredAnnotations(classNames);
        } catch (IOException e) {
            return new AspectIgnoredAnnotations(Arrays.asList(
                    Override.class.getName(),
                    PostConstruct.class.getName(),
                    PreDestroy.class.getName(),
                    Singleton.class.getName(),
                    Inject.class.getName()
            ));
        }
    }

    public boolean isIgnored(TypeMirror typeMirror) {
        Element element = ProcessorContext.getTypes().asElement(typeMirror);
        return isIgnored(element);
    }

    public boolean isIgnored(Element element) {
        if (element instanceof TypeElement) {
            TypeElement typeElement = (TypeElement) element;
            for (String className : classNames) {
                if (className.equals(typeElement.getQualifiedName().toString())
                        || className.equals(typeElement.getSimpleName().toString())) {
                    return true;
                }
            }
        }

        for (String className : classNames) {
            if (className.equals(element.getSimpleName().toString())) {
                return true;
            }
        }

        return false;
    }
}
