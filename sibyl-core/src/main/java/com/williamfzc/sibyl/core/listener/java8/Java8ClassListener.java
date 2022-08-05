package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.listener.java8.base.Java8ClazzLayerListener;
import com.williamfzc.sibyl.core.model.clazz.Clazz;

public class Java8ClassListener extends Java8ClazzLayerListener<Clazz> {
    @Override
    public void enterClassDeclarationWithoutMethodBody(
            Java8Parser.ClassDeclarationWithoutMethodBodyContext ctx) {
        super.enterClassDeclarationWithoutMethodBody(ctx);
        Clazz clazz = getCurrentClazz();
        if (null != clazz) {
            this.storage.save(clazz);
        }
    }
}
