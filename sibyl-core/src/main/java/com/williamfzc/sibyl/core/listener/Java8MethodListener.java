package com.williamfzc.sibyl.core.listener;

import com.williamfzc.sibyl.core.antlr4.*;
import com.williamfzc.sibyl.core.intf.IStorableListener;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.utils.Log;
import java.io.File;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class Java8MethodListener extends Java8BaseListener implements IStorableListener<Method> {
    private Storage<Method> storage = null;

    @Override
    public Storage<Method> getStorage() {
        return storage;
    }

    @Override
    public void setStorage(Storage<Method> storage) {
        this.storage = storage;
    }

    @Override
    public void enterPackageDeclaration(Java8Parser.PackageDeclarationContext ctx) {
        // a list, [cases, java8] == cases.java8
        Log.info("pkg decl: " + ctx.Identifier().toString());
    }

    // use a stack to manage current class
    @Override
    public void enterClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
        Log.info("class decl: " + ctx.normalClassDeclaration().Identifier().getText());
    }

    @Override
    public void exitClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
        Log.info("class decl end: " + ctx.normalClassDeclaration().Identifier().getText());
    }

    // use a stack to manage current method
    @Override
    public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        Log.info("method decl: " + ctx.methodHeader().methodDeclarator().Identifier().getText());
        Method m = new Method();
        m.setId(1L);

        if (this.storage == null) {
            Log.info("storage null!!1");
        }
        this.storage.save(m);
    }

    @Override
    public void exitMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        Log.info(
                "method decl end: " + ctx.methodHeader().methodDeclarator().Identifier().getText());
    }

    @Override
    public void enterFieldDeclaration(Java8Parser.FieldDeclarationContext ctx) {
        Log.info(
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
        walker.walk(this, tree);
    }

    @Override
    public boolean accept(File file) {
        return file.getName().endsWith(".java");
    }
}
