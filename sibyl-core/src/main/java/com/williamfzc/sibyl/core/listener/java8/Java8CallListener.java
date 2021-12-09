package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.model.edge.Edge;
import com.williamfzc.sibyl.core.utils.Log;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import org.antlr.v4.runtime.tree.TerminalNode;

@Data
class MethodUnit {
    private String callerType;
    private String methodName;
}

public class Java8CallListener extends Java8MethodListener<Edge> {
    private final Set<String> headlessMethodSet = new HashSet<>();
    private final Set<MethodUnit> needGuessMethodSet = new HashSet<>();
    private final Set<MethodUnit> readyMethodSet = new HashSet<>();

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
        if (null == typeNameContext) {
            headlessMethodSet.add(methodName);
            return;
        }
        // found a caller
        MethodUnit unit = new MethodUnit();
        unit.setCallerType(typeNameContext.getText());
        unit.setMethodName(methodName);
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
        if (null == typeNameContext) {
            headlessMethodSet.add(methodName);
            return;
        }
        // found a caller
        MethodUnit unit = new MethodUnit();
        unit.setCallerType(typeNameContext.getText());
        unit.setMethodName(methodName);
        needGuessMethodSet.add(unit);
    }

    @Override
    public void afterHandle() {
        // guess and save them to storage
        processHeadlessMethods();
        debugDisplayMethods();
    }

    private void processHeadlessMethods() {}

    private void debugDisplayMethods() {
        headlessMethodSet.forEach(
                each -> {
                    Log.info("headless method: " + each);
                });
        needGuessMethodSet.forEach(
                each -> {
                    Log.info("need guess method: " + each);
                });
        readyMethodSet.forEach(
                each -> {
                    Log.info("ready method: " + each);
                });
    }
}
