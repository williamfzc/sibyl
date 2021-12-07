package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.utils.Log;

public class Java8MethodListener extends Java8StorableListener {
    public void enterPackageDeclaration(
            com.williamfzc.sibyl.core.antlr4.Java8Parser.PackageDeclarationContext ctx) {
        // a list, [cases, java8] == cases.java8
        Log.info("pkg decl: " + ctx.Identifier().toString());
    }

    // use a stack to manage current class
    public void enterClassDeclaration(
            com.williamfzc.sibyl.core.antlr4.Java8Parser.ClassDeclarationContext ctx) {
        Log.info("class decl: " + ctx.normalClassDeclaration().Identifier().getText());
    }

    public void exitClassDeclaration(
            com.williamfzc.sibyl.core.antlr4.Java8Parser.ClassDeclarationContext ctx) {
        Log.info("class decl end: " + ctx.normalClassDeclaration().Identifier().getText());
    }

    // use a stack to manage current method
    public void enterMethodDeclaration(
            com.williamfzc.sibyl.core.antlr4.Java8Parser.MethodDeclarationContext ctx) {
        Log.info("method decl: " + ctx.methodHeader().methodDeclarator().Identifier().getText());
        Method m = new Method();
        m.setId(1L);

        if (this.storage == null) {
            Log.info("storage null!!1");
        }
        this.storage.save(m);
    }

    public void exitMethodDeclaration(
            com.williamfzc.sibyl.core.antlr4.Java8Parser.MethodDeclarationContext ctx) {
        Log.info(
                "method decl end: " + ctx.methodHeader().methodDeclarator().Identifier().getText());
    }

    public void enterFieldDeclaration(
            com.williamfzc.sibyl.core.antlr4.Java8Parser.FieldDeclarationContext ctx) {
        Log.info(
                String.format(
                        "field decl, type: %s, value: %s",
                        ctx.unannType().getText(), ctx.variableDeclaratorList().getText()));
    }
}
