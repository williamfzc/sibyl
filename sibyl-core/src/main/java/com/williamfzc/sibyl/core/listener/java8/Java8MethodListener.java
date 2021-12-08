package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.model.method.MethodBelonging;
import com.williamfzc.sibyl.core.model.method.MethodInfo;
import com.williamfzc.sibyl.core.utils.Log;
import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.tree.ParseTree;

public class Java8MethodListener extends Java8StorableListener {
    private String curPackage;
    private final Deque<String> curClassStack = new LinkedList<>();

    @Override
    public void enterPackageDeclaration(Java8Parser.PackageDeclarationContext ctx) {
        String declaredPackage =
                ctx.Identifier().stream().map(ParseTree::getText).collect(Collectors.joining("."));
        Log.info("pkg decl: " + declaredPackage);
        curPackage = declaredPackage;
    }

    // use a stack to manage current class
    @Override
    public void enterClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
        String declaredClass = ctx.normalClassDeclaration().Identifier().getText();
        Log.info("class decl: " + declaredClass);
        curClassStack.push(declaredClass);
    }

    @Override
    public void exitClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
        String declaredClass = ctx.normalClassDeclaration().Identifier().getText();
        Log.info("class decl end: " + declaredClass);
        curClassStack.pop();
    }

    // use a stack to manage current method
    @Override
    public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        String declaredMethod = ctx.methodHeader().methodDeclarator().Identifier().getText();
        Log.info("method decl: " + declaredMethod);
        String curClass = curClassStack.peekLast();
        Method m = new Method();

        MethodInfo info = new MethodInfo();
        info.setName(declaredMethod);

        MethodBelonging belonging = new MethodBelonging();
        belonging.setPackageName(curPackage);
        belonging.setClassName(curClass);

        m.setInfo(info);
        m.setBelongsTo(belonging);

        this.storage.save(m);
    }

    @Override
    public void exitMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        String declaredMethod = ctx.methodHeader().methodDeclarator().Identifier().getText();
        Log.info("method decl end: " + declaredMethod);
    }

    @Override
    public void enterFieldDeclaration(Java8Parser.FieldDeclarationContext ctx) {
        String declaredField = ctx.unannType().getText();
        String declaredValue = ctx.variableDeclaratorList().getText();
        Log.info(String.format("field decl, type: %s, value: %s", declaredField, declaredValue));
    }
}
