package com.wiredi.compiler;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ThreadSafeElements implements Elements {

    private final Elements delegate;
    private final Object lock = new Object();

    public ThreadSafeElements(Elements delegate) {
        this.delegate = delegate;
    }

    @Override
    public PackageElement getPackageElement(CharSequence name) {
        synchronized (lock) {
            return delegate.getPackageElement(name);
        }
    }

    @Override
    public PackageElement getPackageElement(ModuleElement module, CharSequence name) {
        synchronized (lock) {
            return delegate.getPackageElement(module, name);
        }
    }

    @Override
    public Set<? extends PackageElement> getAllPackageElements(CharSequence name) {
        synchronized (lock) {
            return delegate.getAllPackageElements(name);
        }
    }

    @Override
    public TypeElement getTypeElement(CharSequence name) {
        synchronized (lock) {
            return delegate.getTypeElement(name);
        }
    }

    @Override
    public TypeElement getTypeElement(ModuleElement module, CharSequence name) {
        synchronized (lock) {
            return delegate.getTypeElement(module, name);
        }
    }

    @Override
    public Set<? extends TypeElement> getAllTypeElements(CharSequence name) {
        synchronized (lock) {
            return delegate.getAllTypeElements(name);
        }
    }

    @Override
    public ModuleElement getModuleElement(CharSequence name) {
        synchronized (lock) {
            return delegate.getModuleElement(name);
        }
    }

    @Override
    public Set<? extends ModuleElement> getAllModuleElements() {
        synchronized (lock) {
            return delegate.getAllModuleElements();
        }
    }

    @Override
    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(AnnotationMirror a) {
        synchronized (lock) {
            return delegate.getElementValuesWithDefaults(a);
        }
    }

    @Override
    public String getDocComment(Element e) {
        synchronized (lock) {
            return delegate.getDocComment(e);
        }
    }

    @Override
    public boolean isDeprecated(Element e) {
        synchronized (lock) {
            return delegate.isDeprecated(e);
        }
    }

    @Override
    public Origin getOrigin(Element e) {
        synchronized (lock) {
            return delegate.getOrigin(e);
        }
    }

    @Override
    public Origin getOrigin(AnnotatedConstruct c, AnnotationMirror a) {
        synchronized (lock) {
            return delegate.getOrigin(c, a);
        }
    }

    @Override
    public Origin getOrigin(ModuleElement m, ModuleElement.Directive directive) {
        synchronized (lock) {
            return delegate.getOrigin(m, directive);
        }
    }

    @Override
    public boolean isBridge(ExecutableElement e) {
        synchronized (lock) {
            return delegate.isBridge(e);
        }
    }

    @Override
    public Name getBinaryName(TypeElement type) {
        synchronized (lock) {
            return delegate.getBinaryName(type);
        }
    }

    @Override
    public PackageElement getPackageOf(Element e) {
        synchronized (lock) {
            return delegate.getPackageOf(e);
        }
    }

    @Override
    public ModuleElement getModuleOf(Element e) {
        synchronized (lock) {
            return delegate.getModuleOf(e);
        }
    }

    @Override
    public List<? extends Element> getAllMembers(TypeElement type) {
        synchronized (lock) {
            return delegate.getAllMembers(type);
        }
    }

    @Override
    public TypeElement getOutermostTypeElement(Element e) {
        synchronized (lock) {
            return delegate.getOutermostTypeElement(e);
        }
    }

    @Override
    public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
        synchronized (lock) {
            return delegate.getAllAnnotationMirrors(e);
        }
    }

    @Override
    public boolean hides(Element hider, Element hidden) {
        synchronized (lock) {
            return delegate.hides(hider, hidden);
        }
    }

    @Override
    public boolean overrides(ExecutableElement overrider, ExecutableElement overridden, TypeElement type) {
        synchronized (lock) {
            return delegate.overrides(overrider, overridden, type);
        }
    }

    @Override
    public String getConstantExpression(Object value) {
        synchronized (lock) {
            return delegate.getConstantExpression(value);
        }
    }

    @Override
    public void printElements(Writer w, Element... elements) {
        synchronized (lock) {
            delegate.printElements(w, elements);
        }
    }

    @Override
    public Name getName(CharSequence cs) {
        synchronized (lock) {
            return delegate.getName(cs);
        }
    }

    @Override
    public boolean isFunctionalInterface(TypeElement type) {
        synchronized (lock) {
            return delegate.isFunctionalInterface(type);
        }
    }

    @Override
    public boolean isAutomaticModule(ModuleElement module) {
        synchronized (lock) {
            return delegate.isAutomaticModule(module);
        }
    }

    @Override
    public RecordComponentElement recordComponentFor(ExecutableElement accessor) {
        synchronized (lock) {
            return delegate.recordComponentFor(accessor);
        }
    }

    @Override
    public boolean isCanonicalConstructor(ExecutableElement e) {
        synchronized (lock) {
            return delegate.isCanonicalConstructor(e);
        }
    }

    @Override
    public boolean isCompactConstructor(ExecutableElement e) {
        synchronized (lock) {
            return delegate.isCompactConstructor(e);
        }
    }

    @Override
    public JavaFileObject getFileObjectOf(Element e) {
        synchronized (lock) {
            return delegate.getFileObjectOf(e);
        }
    }
}