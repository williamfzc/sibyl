package com.williamfzc.sibyl.ext.casegen.exporter.junit.wrapper;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public interface BaseWrapper {
    void wrapMethodAnnotation(MethodSpec.Builder methodBuilder);
    void wrapField(FieldSpec.Builder fieldBuilder);
    void wrapClazzAnnotation(TypeSpec.Builder clazzBuilder);
    void wrapJudgeMethod(MethodSpec.Builder methodBuilder);
}
