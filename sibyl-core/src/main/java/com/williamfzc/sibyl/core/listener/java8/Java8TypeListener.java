package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.clazz.ClazzBelonging;
import com.williamfzc.sibyl.core.model.clazz.ClazzBelongingFile;
import com.williamfzc.sibyl.core.model.pkg.Pkg;
import com.williamfzc.sibyl.core.utils.SibylUtils;

public class Java8TypeListener extends Java8ClassListener {
    @Override
    public void enterImportDeclaration(Java8Parser.ImportDeclarationContext ctx) {
        // need not call super
        Java8Parser.SingleTypeImportDeclarationContext declCtx = ctx.singleTypeImportDeclaration();
        if (null == declCtx) {
            return;
        }
        String typeDecl = declCtx.typeName().getText();
        String typeName = SibylUtils.fullPath2ClazzName(typeDecl);

        Clazz clazz = new Clazz();
        clazz.setName(typeName);
        ClazzBelonging belonging = new ClazzBelonging();
        Pkg pkg = new Pkg();
        pkg.setName(SibylUtils.fullPath2PackageName(typeDecl));
        belonging.setPkg(pkg);
        ClazzBelongingFile belongingFile = new ClazzBelongingFile();
        belongingFile.setName(curFile.getPath());
        belonging.setFile(belongingFile);
        clazz.setBelongsTo(belonging);
        this.storage.save(clazz);
    }
}
