package com.williamfzc.sibyl.core.listener.java8.base;

import com.williamfzc.sibyl.core.listener.Java8Lexer;
import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.clazz.ClazzBelonging;
import com.williamfzc.sibyl.core.model.clazz.ClazzBelongingFile;
import com.williamfzc.sibyl.core.model.pkg.Pkg;
import com.williamfzc.sibyl.core.utils.SibylLog;
import com.williamfzc.sibyl.core.utils.SibylUtils;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class Java8ClazzLayerListener<T> extends Java8PackageLayerListener<T> {
    protected final Deque<Clazz> curClassStack = new LinkedList<>();

    // use a stack to manage current class
    @Override
    public void enterClassDeclarationWithoutMethodBody(
            Java8Parser.ClassDeclarationWithoutMethodBodyContext ctx) {
        Java8Parser.NormalClassDeclarationWithoutMethodBodyContext normalClassDeclarationContext =
                ctx.normalClassDeclarationWithoutMethodBody();
        Java8Parser.EnumDeclarationContext enumDeclarationContext = ctx.enumDeclaration();
        // normal or enum
        if (null != normalClassDeclarationContext) {
            curClassStack.push(generateClazz(normalClassDeclarationContext));
            return;
        }
        if (null != enumDeclarationContext) {
            curClassStack.push(generateClazz(enumDeclarationContext));
        }
    }

    @Override
    public void exitClassDeclarationWithoutMethodBody(
            Java8Parser.ClassDeclarationWithoutMethodBodyContext ctx) {
        Java8Parser.NormalClassDeclarationWithoutMethodBodyContext normalClassDeclarationContext =
                ctx.normalClassDeclarationWithoutMethodBody();
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
            // ignore annotationTypeDeclaration
            return;
        }
        SibylLog.debug("interface decl start: " + normalInterfaceDeclarationContext);
        curClassStack.push(generateClazz(normalInterfaceDeclarationContext));
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

    protected Clazz generateClazz(Java8Parser.NormalClassDeclarationWithoutMethodBodyContext ctx) {
        Java8Parser.SuperclassContext superclassContext = ctx.superclass();
        Java8Parser.SuperinterfacesContext superinterfacesContext = ctx.superinterfaces();
        String superClazz = null;
        Set<String> superInterfaces = null;
        if (null != superclassContext) {
            superClazz = superclassContext.classType().getText();
        }
        if (null != superinterfacesContext) {
            superInterfaces =
                    superinterfacesContext.interfaceTypeList().interfaceType().stream()
                            .map(each -> each.classType().getText())
                            .collect(Collectors.toSet());
        }

        return generateClazz(
                ctx.Identifier().getText(),
                ctx.classModifier().stream().map(RuleContext::getText).collect(Collectors.toList()),
                superClazz,
                superInterfaces,
                ctx.start.getLine(),
                ctx.stop.getLine());
    }

    protected Clazz generateClazz(Java8Parser.EnumDeclarationContext ctx) {
        Java8Parser.SuperinterfacesContext superinterfacesContext = ctx.superinterfaces();
        Set<String> superInterfaces = null;
        if (null != superinterfacesContext) {
            superInterfaces =
                    superinterfacesContext.interfaceTypeList().interfaceType().stream()
                            .map(each -> each.classType().getText())
                            .collect(Collectors.toSet());
        }

        return generateClazz(
                ctx.Identifier().getText(),
                ctx.classModifier().stream().map(RuleContext::getText).collect(Collectors.toList()),
                null,
                superInterfaces,
                ctx.start.getLine(),
                ctx.stop.getLine());
    }

    protected Clazz generateClazz(Java8Parser.NormalInterfaceDeclarationContext ctx) {
        Set<String> superInterfaces = null;
        Java8Parser.ExtendsInterfacesContext extendsInterfacesContext = ctx.extendsInterfaces();
        if (null != extendsInterfacesContext) {
            superInterfaces =
                    extendsInterfacesContext.interfaceTypeList().interfaceType().stream()
                            .map(each -> each.classType().getText())
                            .collect(Collectors.toSet());
        }
        return generateClazz(
                ctx.Identifier().getText(),
                ctx.interfaceModifier().stream()
                        .map(RuleContext::getText)
                        .collect(Collectors.toList()),
                null,
                superInterfaces,
                ctx.start.getLine(),
                ctx.stop.getLine());
    }

    protected Clazz generateClazz(
            String clazzName,
            List<String> modifiers,
            String superClazz,
            Set<String> superinterfaces,
            int startLine,
            int endLine) {
        Clazz clazz = new Clazz();
        clazz.setModifier(modifiers);

        clazz.setName(fixClazzName(clazzName));
        Pkg pkg = new Pkg();
        pkg.setName(curPackage);

        ClazzBelongingFile clazzBelongingFile = new ClazzBelongingFile();
        clazzBelongingFile.setName(curFile.getPath());
        clazzBelongingFile.setStartLine(startLine);
        clazzBelongingFile.setEndLine(endLine);

        ClazzBelonging clazzBelonging = new ClazzBelonging();
        clazzBelonging.setPkg(pkg);
        clazzBelonging.setFile(clazzBelongingFile);

        clazz.setBelongsTo(clazzBelonging);

        // super
        clazz.setSuperName(superClazz);
        clazz.setInterfaces(superinterfaces);
        return clazz;
    }

    private String fixClazzName(String clazzName) {
        if (!SibylUtils.isGenerics(clazzName)) {
            return clazzName;
        }
        String originRawType = SibylUtils.generics2raw(clazzName);
        String originParamType = SibylUtils.generics2Param(clazzName);

        String raw = fieldTypeMapping.getOrDefault(originRawType, originRawType);
        String paramType;

        if (originParamType.contains(",")) {
            // more than one
            paramType =
                    Arrays.stream(originParamType.split(","))
                            .map(String::trim)
                            .map(this::fixClazzName)
                            .collect(Collectors.joining(", "));

        } else {
            paramType = fixClazzName(originParamType);
        }
        return String.format("%s<%s>", raw, paramType);
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

    public Clazz getCurrentClazz() {
        return curClassStack.peekLast();
    }
}
