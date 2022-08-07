package com.williamfzc.sibyl.core.listener.golang;

import com.williamfzc.sibyl.core.listener.GoLexer;
import com.williamfzc.sibyl.core.listener.GoParser;
import com.williamfzc.sibyl.core.listener.golang.base.GoStorableListener;
import com.williamfzc.sibyl.core.model.method.Method;
import java.io.File;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class GoSnapshotListener extends GoStorableListener<Method> {
    // functions are non-receiver methods

    @Override
    public void enterMethodDecl(GoParser.MethodDeclContext ctx) {
        // todo
        super.enterMethodDecl(ctx);
    }

    @Override
    public void enterFunctionDecl(GoParser.FunctionDeclContext ctx) {
        // todo
        super.enterFunctionDecl(ctx);
    }

    @Override
    public void exitMethodDecl(GoParser.MethodDeclContext ctx) {
        super.exitMethodDecl(ctx);
    }

    @Override
    public void realHandle(File file, String content) {
        curFile = file;
        GoLexer lexer = new GoLexer(CharStreams.fromString(content));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        GoParser parser = new GoParser(tokens);
        ParseTree tree = parser.sourceFile();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, tree);
    }
}
