package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.intf.IStorableListener;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.storage.Storage;
import java.io.File;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

class Java8StorableListener extends com.williamfzc.sibyl.core.antlr4.Java8BaseListener
        implements IStorableListener<Method> {
    protected Storage<Method> storage = null;

    @Override
    public Storage<Method> getStorage() {
        return storage;
    }

    @Override
    public void setStorage(Storage<Method> storage) {
        this.storage = storage;
    }

    public void handleContent(String content) {
        com.williamfzc.sibyl.core.antlr4.Java8Lexer lexer =
                new com.williamfzc.sibyl.core.antlr4.Java8Lexer(CharStreams.fromString(content));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        com.williamfzc.sibyl.core.antlr4.Java8Parser parser =
                new com.williamfzc.sibyl.core.antlr4.Java8Parser(tokens);
        ParseTree tree = parser.compilationUnit();

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk((ParseTreeListener) this, tree);
    }

    @Override
    public boolean accept(File file) {
        return file.getName().endsWith(".java");
    }
}
