package com.williamfzc.sibyl.core.listener;

import com.williamfzc.sibyl.core.antlr4.*;
import com.williamfzc.sibyl.core.model.Listenable;
import java.io.File;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class Java8MethodListener extends Java8BaseListener implements Listenable {
    private void print(String msg) {
        System.out.println("[listener] " + msg);
    }

    @Override
    public void enterPackageDeclaration(Java8Parser.PackageDeclarationContext ctx) {
        // a list, [cases, java8] == cases.java8
        print("pkg decl: " + ctx.Identifier().toString());
    }

    // use a stack to manage current class
    @Override
    public void enterClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
        print("class decl: " + ctx.normalClassDeclaration().Identifier().getText());
    }

    @Override
    public void exitClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
        print("class decl end: " + ctx.normalClassDeclaration().Identifier().getText());
    }

    // use a stack to manage current method
    @Override
    public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        print("method decl: " + ctx.methodHeader().methodDeclarator().Identifier().getText());
    }

    @Override
    public void exitMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        print("method decl end: " + ctx.methodHeader().methodDeclarator().Identifier().getText());
    }

    @Override
    public void enterFieldDeclaration(Java8Parser.FieldDeclarationContext ctx) {
        print(
                String.format(
                        "field decl, type: %s, value: %s",
                        ctx.unannType().getText(), ctx.variableDeclaratorList().getText()));
    }

    public void handleContent(String content) {
        Java8Lexer lexer = new Java8Lexer(CharStreams.fromString(content));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Java8Parser parser = new Java8Parser(tokens);
        ParseTree tree = parser.compilationUnit();

        ParseTreeWalker walker = new ParseTreeWalker();
        Java8MethodListener listener = new Java8MethodListener();
        walker.walk(listener, tree);
    }

    @Override
    public boolean accept(File file) {
        return file.getName().endsWith(".java");
    }
}
