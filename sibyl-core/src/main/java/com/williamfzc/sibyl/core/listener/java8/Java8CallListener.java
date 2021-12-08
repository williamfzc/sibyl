package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.model.edge.Edge;
import com.williamfzc.sibyl.core.utils.Log;
import org.antlr.v4.runtime.tree.TerminalNode;

public class Java8CallListener extends Java8MethodListener<Edge> {
    @Override
    public void enterMethodInvocation(Java8Parser.MethodInvocationContext ctx) {
        TerminalNode node = ctx.Identifier();
        if (null == node) {
            return;
        }
        String methodName = node.getText();

        // caller?
        if (ctx.primary() != null) {
            Log.info("primary: " + ctx.primary().getText());
        }

        Java8Parser.TypeNameContext caller = ctx.typeName();
        String callerName = "";
        if (null != caller) {
            callerName = caller.getText();
        }
        Log.info(String.format("%s invoke %s", callerName, methodName));
    }

    @Override
    public void enterMethodInvocation_lfno_primary(
            Java8Parser.MethodInvocation_lfno_primaryContext ctx) {
        TerminalNode node = ctx.Identifier();
        if (null == node) {
            return;
        }
        String methodName = node.getText();

        // caller?
        Java8Parser.TypeNameContext caller = ctx.typeName();
        String callerName = "";
        if (null != caller) {
            callerName = caller.getText();
        }
        Log.info(String.format("no primary: %s invoke %s", callerName, methodName));
    }
}
