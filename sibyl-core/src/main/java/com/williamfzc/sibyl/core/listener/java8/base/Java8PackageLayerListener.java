package com.williamfzc.sibyl.core.listener.java8.base;

import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.utils.SibylLog;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.tree.ParseTree;

public abstract class Java8PackageLayerListener<T> extends Java8StorableListener<T> {
    protected String curPackage;

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
}
