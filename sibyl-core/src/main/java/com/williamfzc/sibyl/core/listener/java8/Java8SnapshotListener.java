package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.model.method.MethodBelonging;
import com.williamfzc.sibyl.core.model.method.MethodBelongingFile;
import com.williamfzc.sibyl.core.model.method.MethodInfo;

public class Java8SnapshotListener extends Java8MethodListener<Method> {
    @Override
    public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        super.enterMethodDeclaration(ctx);
        this.storage.save(generateMethod(ctx));
    }

    private Method generateMethod(Java8Parser.MethodDeclarationContext ctx) {
        String curClass = curClassStack.peekLast();
        Method m = new Method();
        MethodInfo info = generateMethodInfo(ctx);

        MethodBelongingFile belongingFile = new MethodBelongingFile();
        belongingFile.setFile(curFile.getPath());
        belongingFile.setStartLine(ctx.methodBody().start.getLine());
        belongingFile.setEndLine(ctx.methodBody().stop.getLine());

        MethodBelonging belonging = new MethodBelonging();
        belonging.setPackageName(curPackage);
        belonging.setClassName(curClass);
        belonging.setFile(belongingFile);

        m.setInfo(info);
        m.setBelongsTo(belonging);
        return m;
    }

    private MethodInfo generateMethodInfo(Java8Parser.MethodDeclarationContext ctx) {
        MethodInfo info = new MethodInfo();
        info.setName(curMethodStack.peekLast());
        info.setReturnType(ctx.methodHeader().result().getText());

        Java8Parser.FormalParameterListContext params =
                ctx.methodHeader().methodDeclarator().formalParameterList();
        if (null != params) {
            // todo parse this list
            // this signature is a raw string now
            info.setSignature(params.getText());
        }
        return info;
    }
}
