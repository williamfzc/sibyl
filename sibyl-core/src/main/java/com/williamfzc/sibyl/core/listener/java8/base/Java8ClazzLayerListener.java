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
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class Java8ClazzLayerListener<T> extends Java8StorableListener<T> {
    protected String curPackage;
    protected final Deque<Clazz> curClassStack = new LinkedList<>();

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

        clazz.setName(fixClazzName(declaredClassName));
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

    protected Clazz generateClazz(Java8Parser.InterfaceDeclarationContext ctx) {
        Java8Parser.NormalInterfaceDeclarationContext normalInterfaceDeclarationContext =
                ctx.normalInterfaceDeclaration();
        if (null == normalInterfaceDeclarationContext) {
            return null;
        }
        Clazz clazz = new Clazz();
        String declaredClassName = normalInterfaceDeclarationContext.Identifier().getText();
        clazz.setName(fixClazzName(declaredClassName));
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
