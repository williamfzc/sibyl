package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.method.*;
import com.williamfzc.sibyl.core.utils.Log;
import java.util.*;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.tree.ParseTree;

public class Java8MethodListener<T> extends Java8StorableListener<T> {
    protected String curPackage;
    protected final Deque<Clazz> curClassStack = new LinkedList<>();
    protected final Deque<Method> curMethodStack = new LinkedList<>();

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
        Java8Parser.NormalClassDeclarationContext normalClassDeclarationContext =
                ctx.normalClassDeclaration();
        if (null == normalClassDeclarationContext) {
            return;
        }
        curClassStack.push(generateClazz(ctx));
    }

    @Override
    public void exitClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
        Java8Parser.NormalClassDeclarationContext normalClassDeclarationContext =
                ctx.normalClassDeclaration();
        if (null == normalClassDeclarationContext) {
            return;
        }
        String declaredClass = normalClassDeclarationContext.Identifier().getText();
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
        curMethodStack.push(generateMethod(ctx));

        // args fields
        Java8Parser.FormalParameterListContext paramsCtx =
                ctx.methodHeader().methodDeclarator().formalParameterList();
        if (null != paramsCtx) {
            Java8Parser.FormalParametersContext formalParametersContext =
                    paramsCtx.formalParameters();
            if (null != formalParametersContext) {
                formalParametersContext
                        .formalParameter()
                        .forEach(
                                each ->
                                        fieldTypeMapping.put(
                                                each.variableDeclaratorId().getText(),
                                                each.unannType().getText()));
            }
        }
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
                        each ->
                                fieldTypeMapping.put(
                                        each.variableDeclaratorId().getText(), declaredType));
    }

    // local vars
    @Override
    public void enterLocalVariableDeclaration(Java8Parser.LocalVariableDeclarationContext ctx) {
        String declaredType = ctx.unannType().getText();
        ctx.variableDeclaratorList()
                .variableDeclarator()
                .forEach(
                        each ->
                                fieldTypeMapping.put(
                                        each.variableDeclaratorId().getText(), declaredType));
    }

    protected Method generateMethod(Java8Parser.MethodDeclarationContext ctx) {
        Clazz curClass = curClassStack.peekLast();
        Method m = new Method();
        MethodInfo info = generateMethodInfo(ctx);

        MethodBelongingFile belongingFile = new MethodBelongingFile();
        belongingFile.setFile(curFile.getPath());
        belongingFile.setStartLine(ctx.methodBody().start.getLine());
        belongingFile.setEndLine(ctx.methodBody().stop.getLine());

        MethodBelonging belonging = new MethodBelonging();
        belonging.setClazz(curClass);
        belonging.setFile(belongingFile);

        m.setInfo(info);
        m.setBelongsTo(belonging);
        return m;
    }

    protected MethodInfo generateMethodInfo(Java8Parser.MethodDeclarationContext ctx) {
        MethodInfo info = new MethodInfo();
        info.setName(ctx.methodHeader().methodDeclarator().Identifier().getText());
        info.setReturnType(ctx.methodHeader().result().getText());

        Java8Parser.FormalParameterListContext params =
                ctx.methodHeader().methodDeclarator().formalParameterList();
        if (null != params) {
            info.setParams(
                    params.formalParameters().formalParameter().stream()
                            .map(
                                    each -> {
                                        Parameter param = new Parameter();
                                        param.setType(each.unannType().getText());
                                        param.setName(each.variableDeclaratorId().getText());
                                        return param;
                                    })
                            .collect(Collectors.toList()));
        }
        return info;
    }

    protected Clazz generateClazz(Java8Parser.ClassDeclarationContext ctx) {
        Java8Parser.NormalClassDeclarationContext normalClassDeclarationContext =
                ctx.normalClassDeclaration();
        if (null == normalClassDeclarationContext) {
            return null;
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
            clazz.setSuperName(superclassContext.classType().getText());
        }
        if (null != superinterfacesContext) {
            clazz.setInterfaces(
                    superinterfacesContext.interfaceTypeList().interfaceType().stream()
                            .map(each -> each.classType().getText())
                            .collect(Collectors.toSet()));
        }
        return clazz;
    }
}
