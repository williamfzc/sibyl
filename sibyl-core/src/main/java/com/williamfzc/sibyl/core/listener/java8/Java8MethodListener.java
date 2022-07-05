package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.clazz.ClazzBelonging;
import com.williamfzc.sibyl.core.model.clazz.ClazzBelongingFile;
import com.williamfzc.sibyl.core.model.method.*;
import com.williamfzc.sibyl.core.model.pkg.Pkg;
import com.williamfzc.sibyl.core.utils.SibylLog;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.antlr.v4.runtime.RuleContext;
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
    public void enterImportDeclaration(Java8Parser.ImportDeclarationContext ctx) {
        Java8Parser.SingleTypeImportDeclarationContext declCtx = ctx.singleTypeImportDeclaration();
        if (null == declCtx) {
            return;
        }
        String typeDecl = declCtx.typeName().getText();
        String[] parts = typeDecl.split("\\.");
        if (parts.length < 1) {
            return;
        }
        String typeName = parts[parts.length - 1];
        fieldTypeMapping.put(typeName, typeDecl);
    }

    @Override
    public void enterPackageDeclaration(Java8Parser.PackageDeclarationContext ctx) {
        String declaredPackage =
                ctx.Identifier().stream().map(ParseTree::getText).collect(Collectors.joining("."));
        SibylLog.debug("pkg decl: " + declaredPackage);
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
        SibylLog.debug("class decl end: " + declaredClass);
        curClassStack.pop();

        // temp
        SibylLog.debug(
                String.format("class %s field mapping: %s", declaredClass, fieldTypeMapping));
    }

    // interface
    @Override
    public void enterInterfaceDeclaration(Java8Parser.InterfaceDeclarationContext ctx) {
        Java8Parser.NormalInterfaceDeclarationContext normalInterfaceDeclarationContext =
                ctx.normalInterfaceDeclaration();
        if (null == normalInterfaceDeclarationContext) {
            return;
        }
        SibylLog.debug("interface decl start: " + normalInterfaceDeclarationContext);
        curClassStack.push(generateClazz(ctx));
    }

    @Override
    public void exitInterfaceDeclaration(Java8Parser.InterfaceDeclarationContext ctx) {
        Java8Parser.NormalInterfaceDeclarationContext normalInterfaceDeclarationContext =
                ctx.normalInterfaceDeclaration();
        if (null == normalInterfaceDeclarationContext) {
            return;
        }
        String declaredClass = normalInterfaceDeclarationContext.Identifier().getText();
        SibylLog.debug("interface decl end: " + declaredClass);
        curClassStack.pop();
    }

    // use a stack to manage current method
    @Override
    public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        String declaredMethod = ctx.methodHeader().methodDeclarator().Identifier().getText();
        SibylLog.debug("method decl: " + declaredMethod);
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
        SibylLog.debug("method decl end: " + declaredMethod);
        curMethodStack.pop();
    }

    @Override
    public void enterInterfaceMethodDeclaration(Java8Parser.InterfaceMethodDeclarationContext ctx) {
        String declaredMethod = ctx.methodHeader().methodDeclarator().Identifier().getText();
        SibylLog.debug("method decl: " + declaredMethod);
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
    public void exitInterfaceMethodDeclaration(Java8Parser.InterfaceMethodDeclarationContext ctx) {
        String declaredMethod = ctx.methodHeader().methodDeclarator().Identifier().getText();
        SibylLog.debug("method decl end: " + declaredMethod);
        curMethodStack.pop();
    }

    // global vars for guess
    @Override
    public void enterFieldDeclaration(Java8Parser.FieldDeclarationContext ctx) {
        String typeName = ctx.unannType().getText();
        final String declaredType = fieldTypeMapping.getOrDefault(typeName, typeName);

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
        belongingFile.setName(curFile.getPath());
        belongingFile.setStartLine(ctx.methodBody().start.getLine());
        belongingFile.setEndLine(ctx.methodBody().stop.getLine());

        MethodBelonging belonging = new MethodBelonging();
        belonging.setClazz(curClass);
        belonging.setFile(belongingFile);

        m.setInfo(info);
        m.setBelongsTo(belonging);
        return m;
    }

    protected Method generateMethod(Java8Parser.InterfaceMethodDeclarationContext ctx) {
        Clazz curClass = curClassStack.peekLast();
        Method m = new Method();
        MethodInfo info = generateMethodInfo(ctx);

        MethodBelongingFile belongingFile = new MethodBelongingFile();
        belongingFile.setName(curFile.getPath());
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
        info.setModifier(
                ctx.methodModifier().stream()
                        .map(RuleContext::getText)
                        .collect(Collectors.toList()));
        Java8Parser.FormalParameterListContext paramsCtx =
                ctx.methodHeader().methodDeclarator().formalParameterList();
        if (null == paramsCtx) {
            return info;
        }
        if ((null == paramsCtx.formalParameters()) && (null == paramsCtx.lastFormalParameter())) {
            return info;
        }
        Stream<Java8Parser.FormalParameterContext> paramStream = Stream.of();
        if (null != paramsCtx.formalParameters()) {
            paramStream =
                    Stream.concat(
                            paramStream, paramsCtx.formalParameters().formalParameter().stream());
        }
        if (null != paramsCtx.lastFormalParameter()) {
            paramStream =
                    Stream.concat(
                            paramStream,
                            Stream.of(paramsCtx.lastFormalParameter().formalParameter()));
        }

        info.setParams(
                paramStream
                        .map(
                                each -> {
                                    Parameter param = new Parameter();
                                    String declType = each.unannType().getText();
                                    declType = fieldTypeMapping.getOrDefault(declType, declType);
                                    param.setType(declType);
                                    param.setName(each.variableDeclaratorId().getText());
                                    return param;
                                })
                        .collect(Collectors.toList()));
        return info;
    }

    protected MethodInfo generateMethodInfo(Java8Parser.InterfaceMethodDeclarationContext ctx) {
        MethodInfo info = new MethodInfo();
        info.setName(ctx.methodHeader().methodDeclarator().Identifier().getText());
        info.setReturnType(ctx.methodHeader().result().getText());
        info.setModifier(
                ctx.interfaceMethodModifier().stream()
                        .map(RuleContext::getText)
                        .collect(Collectors.toList()));
        Java8Parser.FormalParameterListContext paramsCtx =
                ctx.methodHeader().methodDeclarator().formalParameterList();
        if ((null == paramsCtx) || (null == paramsCtx.formalParameters())) {
            return info;
        }

        info.setParams(
                Stream.concat(
                                paramsCtx.formalParameters().formalParameter().stream(),
                                Stream.of(paramsCtx.lastFormalParameter().formalParameter()))
                        .map(
                                each -> {
                                    Parameter param = new Parameter();
                                    String declType = each.unannType().getText();
                                    declType = fieldTypeMapping.getOrDefault(declType, declType);
                                    param.setType(declType);
                                    param.setName(each.variableDeclaratorId().getText());
                                    return param;
                                })
                        .collect(Collectors.toList()));
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
        clazz.setModifier(
                ctx.normalClassDeclaration().classModifier().stream()
                        .map(RuleContext::getText)
                        .collect(Collectors.toList()));

        clazz.setName(declaredClassName);
        Pkg pkg = new Pkg();
        pkg.setName(curPackage);

        ClazzBelongingFile clazzBelongingFile = new ClazzBelongingFile();
        clazzBelongingFile.setName(curFile.getPath());
        clazzBelongingFile.setStartLine(ctx.start.getLine());
        clazzBelongingFile.setEndLine(ctx.stop.getLine());

        ClazzBelonging clazzBelonging = new ClazzBelonging();
        clazzBelonging.setPkg(pkg);
        clazzBelonging.setFile(clazzBelongingFile);

        clazz.setBelongsTo(clazzBelonging);

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

    protected Clazz generateClazz(Java8Parser.InterfaceDeclarationContext ctx) {
        Java8Parser.NormalInterfaceDeclarationContext normalInterfaceDeclarationContext =
                ctx.normalInterfaceDeclaration();
        if (null == normalInterfaceDeclarationContext) {
            return null;
        }
        Clazz clazz = new Clazz();
        String declaredClassName = normalInterfaceDeclarationContext.Identifier().getText();
        clazz.setName(declaredClassName);
        clazz.setModifier(
                ctx.normalInterfaceDeclaration().interfaceModifier().stream()
                        .map(RuleContext::getText)
                        .collect(Collectors.toList()));

        Pkg pkg = new Pkg();
        pkg.setName(curPackage);

        ClazzBelongingFile clazzBelongingFile = new ClazzBelongingFile();
        clazzBelongingFile.setName(curFile.getPath());
        clazzBelongingFile.setStartLine(ctx.start.getLine());
        clazzBelongingFile.setEndLine(ctx.stop.getLine());

        ClazzBelonging clazzBelonging = new ClazzBelonging();
        clazzBelonging.setPkg(pkg);
        clazzBelonging.setFile(clazzBelongingFile);

        clazz.setBelongsTo(clazzBelonging);

        // super
        Java8Parser.ExtendsInterfacesContext superinterfacesContext =
                normalInterfaceDeclarationContext.extendsInterfaces();

        if (null != superinterfacesContext) {
            clazz.setInterfaces(
                    superinterfacesContext.interfaceTypeList().interfaceType().stream()
                            .map(each -> each.classType().getText())
                            .collect(Collectors.toSet()));
        }
        return clazz;
    }
}
