package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.model.clazz.Clazz;

public class Java8ClassListener extends Java8MethodListener<Clazz> {
    @Override
    public void enterClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
        super.enterClassDeclaration(ctx);
        Clazz clazz = generateClazz(ctx);
        if (null != clazz) {
            this.storage.save(clazz);
        }
    }
}
