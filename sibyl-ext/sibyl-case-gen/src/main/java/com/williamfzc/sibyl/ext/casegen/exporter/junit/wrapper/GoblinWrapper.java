package com.williamfzc.sibyl.ext.casegen.exporter.junit.wrapper;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.williamfzc.sibyl.ext.casegen.exporter.junit.Consts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

public class GoblinWrapper extends BaseWrapper{
    @Override
    public void wrapMethodAnnotation(MethodSpec.Builder methodBuilder) {
        methodBuilder.addAnnotation(Test.class);
    }

    @Override
    public void wrapField(FieldSpec.Builder fieldBuilder) {
        fieldBuilder.addAnnotation(Resource.class);
    }

    @Override
    public void wrapClazzAnnotation(TypeSpec.Builder clazzBuilder) {
        // extends baseCase
        clazzBuilder.superclass(ClassName.bestGuess("com.heytap.cpc.dfoob.goblin.core.GoblinBaseTest"));
    }

    @Override
    public void wrapJudgeMethod(MethodSpec.Builder methodBuilder) {
        methodBuilder.addStatement("$T.assertEquals($S, $S)", Assertions.class, Consts.NAME_FIELD_ACTUAL, Consts.NAME_FIELD_EXPECT);
    }
}
