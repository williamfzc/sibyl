package com.williamfzc.sibyl.core.listener.kt;

import com.williamfzc.sibyl.core.listener.KotlinParser;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.utils.Log;

public class KtSnapshotListener extends KtMethodListener<Method> {
    @Override
    public void enterClassMemberDeclaration(KotlinParser.ClassMemberDeclarationContext ctx) {
        super.enterClassMemberDeclaration(ctx);
        Log.info(
                "class member: "
                        + ctx.declaration().functionDeclaration().simpleIdentifier().getText());
        this.storage.save(curMethodStack.peekLast());
    }

    @Override
    public void enterFunctionDeclaration(KotlinParser.FunctionDeclarationContext ctx) {
        super.enterFunctionDeclaration(ctx);
        this.storage.save(curMethodStack.peekLast());
    }

    @Override
    public void enterObjectDeclaration(KotlinParser.ObjectDeclarationContext ctx) {
        super.enterObjectDeclaration(ctx);
        ctx.classBody()
                .classMemberDeclarations()
                .classMemberDeclaration()
                .forEach(
                        each -> {
                            Log.info(
                                    "found decl in object: "
                                            + each.declaration().functionDeclaration().getText());
                            this.enterClassMemberDeclaration(each);
                        });
    }
}
