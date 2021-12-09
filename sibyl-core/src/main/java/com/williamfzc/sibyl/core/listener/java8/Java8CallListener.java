package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.model.edge.Edge;
import com.williamfzc.sibyl.core.model.edge.RawEdge;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.utils.Log;
import java.util.HashSet;
import java.util.Set;
import org.antlr.v4.runtime.tree.TerminalNode;

public class Java8CallListener extends Java8MethodListener<Edge> {
    // todo: should collect type arguments too
    private final Set<RawEdge> headlessMethodSet = new HashSet<>();
    private final Set<RawEdge> needGuessMethodSet = new HashSet<>();
    private final Set<RawEdge> readyMethodSet = new HashSet<>();
    private final Set<Method> declaredMethods = new HashSet<>();
    private final Set<Edge> uncheckedEdges = new HashSet<>();
    private Method curMethod = null;

    @Override
    public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        super.enterMethodDeclaration(ctx);
        declaredMethods.add(generateMethod(ctx));
        curMethod = generateMethod(ctx);
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

        RawEdge rawEdge = new RawEdge();
        rawEdge.setFromMethodName(curMethodStack.peekLast());
        if (null == typeNameContext) {
            rawEdge.setToMethodName(methodName);
            headlessMethodSet.add(rawEdge);
        } else {
            // found a caller
            rawEdge.setCallerType(typeNameContext.getText());
            rawEdge.setToMethodName(methodName);
            needGuessMethodSet.add(rawEdge);
        }

        Edge edge = new Edge();
        edge.setSource(curMethod);
        edge.setRawEdge(rawEdge);
        uncheckedEdges.add(edge);
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
        RawEdge rawEdge = new RawEdge();
        rawEdge.setFromMethodName(curMethodStack.peekLast());
        if (null == typeNameContext) {
            rawEdge.setToMethodName(methodName);
            headlessMethodSet.add(rawEdge);
        } else {
            // found a caller
            rawEdge.setCallerType(typeNameContext.getText());
            rawEdge.setToMethodName(methodName);
            needGuessMethodSet.add(rawEdge);
        }

        Edge edge = new Edge();
        edge.setSource(curMethod);
        edge.setRawEdge(rawEdge);
        uncheckedEdges.add(edge);
    }

    @Override
    public void afterHandle() {
        // guess and save them to storage
        processHeadlessMethods();
        processNeedGuestMethods();
        debugDisplayMethods();
        this.storage.save(uncheckedEdges);
    }

    private void processHeadlessMethods() {
        headlessMethodSet.forEach(
                eachRawEdge -> {
                    // declared?
                    declaredMethods.forEach(
                            eachDeclaredMethod -> {
                                if (eachDeclaredMethod
                                        .getInfo()
                                        .getName()
                                        .equals(eachRawEdge.getToMethodName())) {
                                    // modify in place
                                    eachRawEdge.setCallerType(
                                            eachDeclaredMethod.getBelongsTo().getClassName());
                                    readyMethodSet.add(eachRawEdge);
                                }
                            });
                });
    }

    private void processNeedGuestMethods() {
        needGuessMethodSet.forEach(
                eachRawEdge -> {
                    String curCaller = eachRawEdge.getCallerType();
                    if (fieldTypeMapping.containsKey(curCaller)) {
                        String realType = fieldTypeMapping.get(curCaller);
                        eachRawEdge.setCallerType(realType);
                        readyMethodSet.add(eachRawEdge);
                    }
                });
    }

    private void debugDisplayMethods() {
        readyMethodSet.forEach(
                each -> {
                    Log.info("ready method: " + each);
                });
        uncheckedEdges.forEach(
                each -> {
                    Log.info("edge: " + each);
                });
    }
}
