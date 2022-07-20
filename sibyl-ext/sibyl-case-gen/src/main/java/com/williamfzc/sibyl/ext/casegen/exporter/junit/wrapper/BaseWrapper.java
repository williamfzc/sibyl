package com.williamfzc.sibyl.ext.casegen.exporter.junit.wrapper;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public abstract class BaseWrapper {
    public abstract void wrapMethodAnnotation(MethodSpec.Builder methodBuilder);
    public abstract void wrapField(FieldSpec.Builder fieldBuilder);
    public abstract void wrapClazzAnnotation(TypeSpec.Builder clazzBuilder);
    public abstract void wrapJudgeMethod(MethodSpec.Builder methodBuilder);
}
