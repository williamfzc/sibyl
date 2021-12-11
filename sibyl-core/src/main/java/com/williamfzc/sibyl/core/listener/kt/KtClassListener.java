package com.williamfzc.sibyl.core.listener.kt;

import com.williamfzc.sibyl.core.listener.KotlinParser;
import com.williamfzc.sibyl.core.model.clazz.Clazz;

public class KtClassListener extends KtMethodListener<Clazz> {
    @Override
    public void enterClassDeclaration(KotlinParser.ClassDeclarationContext ctx) {
        super.enterClassDeclaration(ctx);
        Clazz clazz = generateClazz(ctx);
        if (null != clazz) {
            this.storage.save(clazz);
        }
    }

    @Override
    public void enterObjectDeclaration(KotlinParser.ObjectDeclarationContext ctx) {
        super.enterObjectDeclaration(ctx);
        Clazz clazz = generateClazz(ctx);
        if (null != clazz) {
            this.storage.save(clazz);
        }
    }
}
