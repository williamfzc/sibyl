package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.listener.Java8Lexer;
import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.listener.java8.base.Java8MethodLayerListener;
import com.williamfzc.sibyl.core.model.edge.Edge;
import com.williamfzc.sibyl.core.model.edge.RawEdge;
import com.williamfzc.sibyl.core.model.method.Method;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

public class Java8CallListener extends Java8MethodLayerListener<Edge> {
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
        if (curMethod != null) {
            rawEdge.setFromMethodName(curMethod.getInfo().getName());
        } else {
            // not belong to any methods
            rawEdge.setFromMethodName(Method.UNKNOWN_NAME);
        }
        rawEdge.setLine(ctx.getStart().getLine());
        // todo: how?
        rawEdge.setType(null);
        rawEdge.setStatement(ctx.getText());
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
        if (curMethod != null) {
            rawEdge.setFromMethodName(curMethod.getInfo().getName());
        } else {
            // not belong to any methods
            rawEdge.setFromMethodName(Method.UNKNOWN_NAME);
        }

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
        super.afterHandle();
        // guess and save them to storage
        processHeadlessMethods();
        processNeedGuestMethods();
        this.storage.save(uncheckedEdges);
    }

    private void processHeadlessMethods() {
        headlessMethodSet.forEach(
                eachRawEdge -> {
                    // let analyzer do the filter
                    declaredMethods.forEach(
                            eachDeclaredMethod -> {
                                // modify in place
                                eachRawEdge.setCallerType(
                                        eachDeclaredMethod.getBelongsTo().getClazz().getFullName());
                                readyMethodSet.add(eachRawEdge);
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
                                .compilationUnit());
    }
}
