package com.github.thorbenkuck.di.processor.utils;

import com.github.thorbenkuck.di.annotations.ManualWireCandidate;
import com.github.thorbenkuck.di.annotations.Wire;

import javax.lang.model.type.TypeMirror;

public class WiredTypeUtils {

    public static boolean isWireCandidate(TypeMirror typeMirror) {
        return typeMirror.getAnnotation(Wire.class) != null
                || typeMirror.getAnnotation(ManualWireCandidate.class) != null;
    }

}
