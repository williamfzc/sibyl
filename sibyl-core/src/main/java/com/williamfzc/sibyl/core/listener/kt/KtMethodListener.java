package com.williamfzc.sibyl.core.listener.kt;

import com.williamfzc.sibyl.core.listener.KotlinParser;
import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.clazz.ClazzBelonging;
import com.williamfzc.sibyl.core.model.clazz.ClazzBelongingFile;
import com.williamfzc.sibyl.core.model.method.*;
import com.williamfzc.sibyl.core.model.pkg.Pkg;
import com.williamfzc.sibyl.core.utils.Log;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

public class KtMethodListener<T> extends KtStorableListener<T> {
    protected String curPackage;
    protected final Deque<Clazz> curClassStack = new LinkedList<>();
    protected final Deque<Method> curMethodStack = new LinkedList<>();

    // todo: what about fields from super class and function args?
    // not a good design now (e.g. nested class
    // k: name, v: type
    protected final Map<String, String> fieldTypeMapping = new HashMap<>();

    // entry
    @Override
    public void enterPackageHeader(KotlinParser.PackageHeaderContext ctx) {
        String declaredPackage =
                ctx.identifier().simpleIdentifier().stream()
                        .map(ParseTree::getText)
                        .collect(Collectors.joining("."));
        Log.info("pkg decl: " + declaredPackage);
        curPackage = declaredPackage;
        fieldTypeMapping.clear();
    }

    @Override
    public void enterClassDeclaration(KotlinParser.ClassDeclarationContext ctx) {
        Log.info("class decl: " + ctx.simpleIdentifier().Identifier().getText());
        curClassStack.push(generateClazz(ctx));
    }

    @Override
    public void enterObjectDeclaration(KotlinParser.ObjectDeclarationContext ctx) {
        Log.info("object decl: " + ctx.simpleIdentifier().Identifier().getText());
        curClassStack.push(generateClazz(ctx));
    }

    @Override
    public void exitObjectDeclaration(KotlinParser.ObjectDeclarationContext ctx) {
        curClassStack.pop();
    }

    @Override
    public void exitClassDeclaration(KotlinParser.ClassDeclarationContext ctx) {
        curClassStack.pop();
    }

    @Override
    public void enterClassMemberDeclaration(KotlinParser.ClassMemberDeclarationContext ctx) {
        KotlinParser.FunctionDeclarationContext methodCtx = ctx.declaration().functionDeclaration();
        curMethodStack.push(generateMethod(methodCtx));
    }

    @Override
    public void exitClassMemberDeclaration(KotlinParser.ClassMemberDeclarationContext ctx) {
        curMethodStack.pop();
    }

    @Override
    public void enterVariableDeclaration(KotlinParser.VariableDeclarationContext ctx) {
        Log.info("field decl: " + ctx.simpleIdentifier().getText());
    }

    protected Clazz generateClazz(KotlinParser.ObjectDeclarationContext ctx) {
        Clazz clazz = new Clazz();
        String declaredClassName = ctx.simpleIdentifier().getText();
        clazz.setName(declaredClassName);
        Pkg pkg = new Pkg();
        pkg.setName(curPackage);

        ClazzBelongingFile clazzBelongingFile = new ClazzBelongingFile();
        clazzBelongingFile.setFile(curFile.getPath());
        clazzBelongingFile.setStartLine(ctx.start.getLine());
        clazzBelongingFile.setEndLine(ctx.stop.getLine());

        ClazzBelonging clazzBelonging = new ClazzBelonging();
        clazzBelonging.setPkg(pkg);
        clazzBelonging.setFile(clazzBelongingFile);

        clazz.setBelongsTo(clazzBelonging);

        // super
        clazz.setSuperName(
                ctx.delegationSpecifiers().annotatedDelegationSpecifier().stream()
                        .map(RuleContext::getText)
                        .collect(Collectors.joining(".")));
        return clazz;
    }

    protected Clazz generateClazz(KotlinParser.ClassDeclarationContext ctx) {
        Clazz clazz = new Clazz();
        String declaredClassName = ctx.simpleIdentifier().getText();
        clazz.setName(declaredClassName);
        Pkg pkg = new Pkg();
        pkg.setName(curPackage);

        ClazzBelongingFile clazzBelongingFile = new ClazzBelongingFile();
        clazzBelongingFile.setFile(curFile.getPath());
        clazzBelongingFile.setStartLine(ctx.start.getLine());
        clazzBelongingFile.setEndLine(ctx.stop.getLine());

        ClazzBelonging clazzBelonging = new ClazzBelonging();
        clazzBelonging.setPkg(pkg);
        clazzBelonging.setFile(clazzBelongingFile);

        clazz.setBelongsTo(clazzBelonging);

        // super
        clazz.setSuperName(
                ctx.delegationSpecifiers().annotatedDelegationSpecifier().stream()
                        .map(RuleContext::getText)
                        .collect(Collectors.joining(".")));
        return clazz;
    }

    protected Method generateMethod(KotlinParser.FunctionDeclarationContext ctx) {
        Log.info("gen mod: " + curClassStack.peekLast());
        Clazz curClass = curClassStack.peekLast();
        Method m = new Method();
        MethodInfo info = generateMethodInfo(ctx);

        MethodBelongingFile belongingFile = new MethodBelongingFile();
        belongingFile.setFile(curFile.getPath());
        belongingFile.setStartLine(ctx.functionBody().start.getLine());
        belongingFile.setEndLine(ctx.functionBody().stop.getLine());

        MethodBelonging belonging = new MethodBelonging();
        belonging.setClazz(curClass);
        belonging.setFile(belongingFile);

        m.setInfo(info);
        m.setBelongsTo(belonging);
        Log.info("gen done: " + m);
        return m;
    }

    protected MethodInfo generateMethodInfo(KotlinParser.FunctionDeclarationContext ctx) {
        MethodInfo info = new MethodInfo();
        info.setName(ctx.simpleIdentifier().getText());

        info.setParams(
                ctx.functionValueParameters().functionValueParameter().stream()
                        .map(
                                each -> {
                                    Parameter parameter = new Parameter();
                                    KotlinParser.ParameterContext parameterContext =
                                            each.parameter();
                                    parameter.setType(parameterContext.type_().getText());
                                    parameter.setName(
                                            parameterContext.simpleIdentifier().getText());
                                    return parameter;
                                })
                        .collect(Collectors.toList()));
        info.setReturnType("");
        return info;
    }
}
