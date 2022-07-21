package com.williamfzc.sibyl.ext.casegen.exporter.junit.wrapper;

import com.squareup.javapoet.*;
import com.williamfzc.sibyl.ext.casegen.exporter.junit.Consts;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

public enum SpringWrapper implements BaseWrapper {
    INSTANCE;

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
        AnnotationSpec.Builder runWithBuilder =
                AnnotationSpec.builder(RunWith.class)
                        .addMember(
                                "value",
                                CodeBlock.builder().add("$T.class", SpringRunner.class).build());

        clazzBuilder.addAnnotation(runWithBuilder.build());
        clazzBuilder.addAnnotation(SpringBootTest.class);
    }

    @Override
    public void wrapJudgeMethod(MethodSpec.Builder methodBuilder) {
        methodBuilder.addStatement("$T.assertEquals($S, $S)", Assert.class, Consts.NAME_FIELD_ACTUAL, Consts.NAME_FIELD_EXPECT);
    }
}
