package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.utils.Log;

public class Java8MethodListener extends Java8StorableListener {
    @Override
    public void enterPackageDeclaration(Java8Parser.PackageDeclarationContext ctx) {
        // a list, [cases, java8] == cases.java8
        Log.info("pkg decl: " + ctx.Identifier().toString());
    }

    // use a stack to manage current class
    @Override
    public void enterClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
        Log.info("class decl: " + ctx.normalClassDeclaration().Identifier().getText());
    }

    @Override
    public void exitClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
        Log.info("class decl end: " + ctx.normalClassDeclaration().Identifier().getText());
    }

    // use a stack to manage current method
    @Override
    public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        Log.info("method decl: " + ctx.methodHeader().methodDeclarator().Identifier().getText());
        Method m = new Method();
        m.setId(1L);

        if (this.storage == null) {
            Log.info("storage null!!1");
        }
        this.storage.save(m);
    }

    @Override
    public void exitMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        Log.info(
                "method decl end: " + ctx.methodHeader().methodDeclarator().Identifier().getText());
    }

    @Override
    public void enterFieldDeclaration(Java8Parser.FieldDeclarationContext ctx) {
        Log.info(
                String.format(
                        "field decl, type: %s, value: %s",
                        ctx.unannType().getText(), ctx.variableDeclaratorList().getText()));
    }
}
