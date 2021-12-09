package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.model.edge.Edge;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.utils.Log;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import org.antlr.v4.runtime.tree.TerminalNode;

// todo: how to convert this unit to Edge?
@Data
class MethodUnit {
    private String fromMethodName;
    private String callerType;
    private String toMethodName;
}

public class Java8CallListener extends Java8MethodListener<Edge> {
    // todo: should collect type arguments too
    private final Set<MethodUnit> headlessMethodSet = new HashSet<>();
    private final Set<MethodUnit> needGuessMethodSet = new HashSet<>();
    private final Set<MethodUnit> readyMethodSet = new HashSet<>();
    private final Set<Method> declaredMethods = new HashSet<>();

    @Override
    public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        super.enterMethodDeclaration(ctx);
        declaredMethods.add(generateMethod(ctx));
    }

    @Override
    public void enterMethodInvocation(Java8Parser.MethodInvocationContext ctx) {
        TerminalNode node = ctx.Identifier();
        // invalid
        if (null == node) {
            return;
        }

        String methodName = node.getText();

        // search the caller
        Java8Parser.TypeNameContext typeNameContext = ctx.typeName();
        MethodUnit unit = new MethodUnit();
        if (null == typeNameContext) {
            unit.setFromMethodName(curMethodStack.peekLast());
            unit.setToMethodName(methodName);
            headlessMethodSet.add(unit);
            return;
        }
        // found a caller
        unit.setFromMethodName(curMethodStack.peekLast());
        unit.setCallerType(typeNameContext.getText());
        unit.setToMethodName(methodName);
        needGuessMethodSet.add(unit);
    }

    @Override
    public void enterMethodInvocation_lfno_primary(
            Java8Parser.MethodInvocation_lfno_primaryContext ctx) {
        TerminalNode node = ctx.Identifier();
        if (null == node) {
            return;
        }
        String methodName = node.getText();

        // search the caller
        Java8Parser.TypeNameContext typeNameContext = ctx.typeName();
        MethodUnit unit = new MethodUnit();
        if (null == typeNameContext) {
            unit.setFromMethodName(curMethodStack.peekLast());
            unit.setToMethodName(methodName);
            headlessMethodSet.add(unit);
            return;
        }
        // found a caller
        unit.setFromMethodName(curMethodStack.peekLast());
        unit.setCallerType(typeNameContext.getText());
        unit.setToMethodName(methodName);
        needGuessMethodSet.add(unit);
    }

    @Override
    public void afterHandle() {
        // guess and save them to storage
        processHeadlessMethods();
        processNeedGuestMethods();
        debugDisplayMethods();
    }

    private void processHeadlessMethods() {
        headlessMethodSet.forEach(
                eachMethod -> {
                    // declared?
                    declaredMethods.forEach(
                            eachDeclaredMethod -> {
                                if (eachDeclaredMethod
                                        .getInfo()
                                        .getName()
                                        .equals(eachMethod.getToMethodName())) {
                                    MethodUnit unit = new MethodUnit();
                                    unit.setFromMethodName(eachMethod.getFromMethodName());
                                    unit.setCallerType(
                                            eachDeclaredMethod.getBelongsTo().getClassName());
                                    unit.setToMethodName(eachMethod.getToMethodName());
                                    readyMethodSet.add(unit);
                                }
                            });
                });
    }

    private void processNeedGuestMethods() {
        needGuessMethodSet.forEach(
                eachMethod -> {
                    String curCaller = eachMethod.getCallerType();
                    if (fieldTypeMapping.containsKey(curCaller)) {
                        String realType = fieldTypeMapping.get(curCaller);
                        MethodUnit unit = new MethodUnit();
                        unit.setFromMethodName(eachMethod.getFromMethodName());
                        unit.setCallerType(realType);
                        unit.setToMethodName(eachMethod.getToMethodName());
                        readyMethodSet.add(unit);
                    }
                });
    }

    private void debugDisplayMethods() {
        readyMethodSet.forEach(
                each -> {
                    Log.info("ready method: " + each);
                });
    }
}
