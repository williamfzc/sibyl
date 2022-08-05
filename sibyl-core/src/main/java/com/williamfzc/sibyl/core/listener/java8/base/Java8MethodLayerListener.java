package com.williamfzc.sibyl.core.listener.java8.base;

import com.williamfzc.sibyl.core.listener.Java8Lexer;
import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.method.*;
import com.williamfzc.sibyl.core.utils.SibylLog;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class Java8MethodLayerListener<T> extends Java8ClazzLayerListener<T> {
    protected final Deque<Method> curMethodStack = new LinkedList<>();

    // use a stack to manage current method
    @Override
    public void enterMethodDeclarationWithoutMethodBody(
            Java8Parser.MethodDeclarationWithoutMethodBodyContext ctx) {
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
    public void exitMethodDeclarationWithoutMethodBody(
            Java8Parser.MethodDeclarationWithoutMethodBodyContext ctx) {
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

    protected Method generateMethod(Java8Parser.MethodDeclarationWithoutMethodBodyContext ctx) {
        Clazz curClass = getCurrentClazz();
        Method m = new Method();
        MethodInfo info = generateMethodInfo(ctx);

        MethodBelongingFile belongingFile = new MethodBelongingFile();
        belongingFile.setName(curFile.getPath());
        belongingFile.setStartLine(ctx.methodBodyEmpty().start.getLine());
        belongingFile.setEndLine(ctx.methodBodyEmpty().stop.getLine());

        MethodBelonging belonging = new MethodBelonging();
        belonging.setClazz(curClass);
        belonging.setFile(belongingFile);

        m.setInfo(info);
        m.setBelongsTo(belonging);
        return m;
    }

    protected Method generateMethod(Java8Parser.MethodDeclarationContext ctx) {
        Clazz curClass = getCurrentClazz();
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
        Clazz curClass = getCurrentClazz();
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

    protected MethodInfo generateMethodInfo(
            Java8Parser.MethodDeclarationWithoutMethodBodyContext ctx) {
        MethodInfo info = new MethodInfo();
        info.setName(ctx.methodHeader().methodDeclarator().Identifier().getText());
        String rawReturnType = ctx.methodHeader().result().getText();
        info.setReturnType(fieldTypeMapping.getOrDefault(rawReturnType, rawReturnType));
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

    protected MethodInfo generateMethodInfo(Java8Parser.MethodDeclarationContext ctx) {
        MethodInfo info = new MethodInfo();
        info.setName(ctx.methodHeader().methodDeclarator().Identifier().getText());
        String rawReturnType = ctx.methodHeader().result().getText();
        info.setReturnType(fieldTypeMapping.getOrDefault(rawReturnType, rawReturnType));
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
        String rawReturnType = ctx.methodHeader().result().getText();
        info.setReturnType(fieldTypeMapping.getOrDefault(rawReturnType, rawReturnType));
        info.setModifier(
                ctx.interfaceMethodModifier().stream()
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

    @Override
    public void realHandle(File file, String content) {
        // overwrite this method for custom parser
        curFile = file;
        new ParseTreeWalker()
                .walk(
                        this,
                        new Java8Parser(
                                        new CommonTokenStream(
                                                new Java8Lexer(CharStreams.fromString(content))))
                                .compilationUnitWithoutMethodBody());
    }
}
