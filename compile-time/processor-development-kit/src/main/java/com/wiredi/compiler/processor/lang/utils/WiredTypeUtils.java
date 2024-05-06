package com.wiredi.compiler.processor.lang.utils;

import com.wiredi.annotations.ManualWireCandidate;
import com.wiredi.annotations.Wire;

import javax.lang.model.type.TypeMirror;

public class WiredTypeUtils {

    public boolean isWireCandidate(TypeMirror typeMirror) {
        return typeMirror.getAnnotation(Wire.class) != null
                || typeMirror.getAnnotation(ManualWireCandidate.class) != null;
    }
}
