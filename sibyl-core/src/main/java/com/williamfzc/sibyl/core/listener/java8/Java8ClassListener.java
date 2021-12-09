package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.model.clazz.Clazz;
import java.util.stream.Collectors;

public class Java8ClassListener extends Java8MethodListener<Clazz> {
    @Override
    public void enterClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
        super.enterClassDeclaration(ctx);
        Java8Parser.NormalClassDeclarationContext normalClassDeclarationContext =
                ctx.normalClassDeclaration();
        if (null == normalClassDeclarationContext) {
            return;
        }
        Clazz clazz = new Clazz();
        String declaredClassName = normalClassDeclarationContext.Identifier().getText();
        clazz.setName(declaredClassName);
        clazz.setPackageName(curPackage);

        // super
        Java8Parser.SuperclassContext superclassContext =
                normalClassDeclarationContext.superclass();
        Java8Parser.SuperinterfacesContext superinterfacesContext =
                normalClassDeclarationContext.superinterfaces();
        if (null != superclassContext) {
            clazz.setSuperName(superclassContext.getText());
        }
        if (null != superinterfacesContext) {
            clazz.setInterfaces(
                    superinterfacesContext.interfaceTypeList().interfaceType().stream()
                            .map(each -> each.classType().getText())
                            .collect(Collectors.toSet()));
        }
        this.storage.save(clazz);
    }
}
