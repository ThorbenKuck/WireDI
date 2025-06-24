package com.wiredi.compiler;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.Types;
import java.util.List;

public class ThreadSafeTypes implements Types {
    
    private final Types delegate;
    private final Object lock = new Object();
    
    public ThreadSafeTypes(Types delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public Element asElement(TypeMirror t) {
        synchronized (lock) {
            return delegate.asElement(t);
        }
    }
    
    @Override
    public boolean isSameType(TypeMirror t1, TypeMirror t2) {
        synchronized (lock) {
            return delegate.isSameType(t1, t2);
        }
    }
    
    @Override
    public boolean isSubtype(TypeMirror t1, TypeMirror t2) {
        synchronized (lock) {
            return delegate.isSubtype(t1, t2);
        }
    }

    @Override
    public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
        synchronized (lock) {
            return delegate.isAssignable(t1, t2);
        }
    }

    @Override
    public boolean contains(TypeMirror t1, TypeMirror t2) {
        synchronized (lock) {
            return delegate.contains(t1, t2);
        }
    }

    @Override
    public boolean isSubsignature(ExecutableType m1, ExecutableType m2) {
        synchronized (lock) {
            return delegate.isSubsignature(m1, m2);
        }
    }

    @Override
    public List<? extends TypeMirror> directSupertypes(TypeMirror t) {
        synchronized (lock) {
            return delegate.directSupertypes(t);
        }
    }

    @Override
    public TypeMirror erasure(TypeMirror t) {
        synchronized (lock) {
            return delegate.erasure(t);
        }
    }

    @Override
    public TypeElement boxedClass(PrimitiveType p) {
        synchronized (lock) {
            return delegate.boxedClass(p);
        }
    }

    @Override
    public PrimitiveType unboxedType(TypeMirror t) {
        synchronized (lock) {
            return delegate.unboxedType(t);
        }
    }

    @Override
    public TypeMirror capture(TypeMirror t) {
        synchronized (lock) {
            return delegate.capture(t);
        }
    }

    @Override
    public PrimitiveType getPrimitiveType(TypeKind kind) {
        synchronized (lock) {
            return delegate.getPrimitiveType(kind);
        }
    }

    @Override
    public NullType getNullType() {
        synchronized (lock) {
            return delegate.getNullType();
        }
    }

    @Override
    public NoType getNoType(TypeKind kind) {
        synchronized (lock) {
            return delegate.getNoType(kind);
        }
    }

    @Override
    public ArrayType getArrayType(TypeMirror componentType) {
        synchronized (lock) {
            return delegate.getArrayType(componentType);
        }
    }

    @Override
    public WildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound) {
        synchronized (lock) {
            return delegate.getWildcardType(extendsBound, superBound);
        }
    }

    @Override
    public DeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs) {
        synchronized (lock) {
            return delegate.getDeclaredType(typeElem, typeArgs);
        }
    }

    @Override
    public DeclaredType getDeclaredType(DeclaredType containing, TypeElement typeElem, TypeMirror... typeArgs) {
        synchronized (lock) {
            return delegate.getDeclaredType(containing, typeElem, typeArgs);
        }
    }

    @Override
    public TypeMirror asMemberOf(DeclaredType containing, Element element) {
        synchronized (lock) {
            return delegate.asMemberOf(containing, element);
        }
    }
}