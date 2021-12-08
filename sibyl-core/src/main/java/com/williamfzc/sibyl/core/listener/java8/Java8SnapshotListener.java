package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.model.method.MethodBelonging;
import com.williamfzc.sibyl.core.model.method.MethodInfo;

public class Java8SnapshotListener extends Java8MethodListener<Method> {
    @Override
    public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        super.enterMethodDeclaration(ctx);
        String curClass = curClassStack.peekLast();
        Method m = new Method();

        MethodInfo info = new MethodInfo();
        info.setName(curMethodStack.peekLast());

        MethodBelonging belonging = new MethodBelonging();
        belonging.setPackageName(curPackage);
        belonging.setClassName(curClass);

        m.setInfo(info);
        m.setBelongsTo(belonging);

        this.storage.save(m);
    }
}
