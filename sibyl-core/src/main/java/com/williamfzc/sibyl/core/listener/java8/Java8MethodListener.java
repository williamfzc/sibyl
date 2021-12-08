package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.utils.Log;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.tree.ParseTree;

public class Java8MethodListener<T> extends Java8StorableListener<T> {
    protected String curPackage;
    protected final Deque<String> curClassStack = new LinkedList<>();
    protected final Deque<String> curMethodStack = new LinkedList<>();

    // todo: what about fields from super class and function args?
    // not a good design now (e.g. nested class
    // k: name, v: type
    protected final Map<String, String> fieldTypeMapping = new HashMap<>();

    // entry
    @Override
    public void enterPackageDeclaration(Java8Parser.PackageDeclarationContext ctx) {
        String declaredPackage =
                ctx.Identifier().stream().map(ParseTree::getText).collect(Collectors.joining("."));
        Log.info("pkg decl: " + declaredPackage);
        curPackage = declaredPackage;
        fieldTypeMapping.clear();
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

        // temp
        Log.info(String.format("class %s field mapping: %s", declaredClass, fieldTypeMapping));
    }

    // use a stack to manage current method
    @Override
    public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        String declaredMethod = ctx.methodHeader().methodDeclarator().Identifier().getText();
        Log.info("method decl: " + declaredMethod);
        curMethodStack.push(declaredMethod);
    }

    @Override
    public void exitMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        String declaredMethod = ctx.methodHeader().methodDeclarator().Identifier().getText();
        Log.info("method decl end: " + declaredMethod);
        curMethodStack.pop();
    }

    // global vars for guess
    @Override
    public void enterFieldDeclaration(Java8Parser.FieldDeclarationContext ctx) {
        String declaredType = ctx.unannType().getText();
        ctx.variableDeclaratorList()
                .variableDeclarator()
                .forEach(
                        each -> {
                            fieldTypeMapping.put(
                                    each.variableDeclaratorId().getText(), declaredType);
                        });
    }

    // local vars
    @Override
    public void enterLocalVariableDeclaration(Java8Parser.LocalVariableDeclarationContext ctx) {
        String declaredType = ctx.unannType().getText();
        ctx.variableDeclaratorList()
                .variableDeclarator()
                .forEach(
                        each -> {
                            fieldTypeMapping.put(
                                    each.variableDeclaratorId().getText(), declaredType);
                        });
    }
}
