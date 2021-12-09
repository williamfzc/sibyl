package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.intf.IStorableListener;
import com.williamfzc.sibyl.core.listener.Java8BaseListener;
import com.williamfzc.sibyl.core.listener.Java8Lexer;
import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.storage.Storage;
import java.io.File;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

class Java8StorableListener<T> extends Java8BaseListener implements IStorableListener<T> {
    protected File curFile = null;
    protected Storage<T> storage = null;

    @Override
    public Storage<T> getStorage() {
        return storage;
    }

    @Override
    public void setStorage(Storage<T> storage) {
        this.storage = storage;
    }

    public void handle(File file, String content) {
        curFile = file;
        Java8Lexer lexer = new Java8Lexer(CharStreams.fromString(content));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Java8Parser parser = new Java8Parser(tokens);
        ParseTree tree = parser.compilationUnit();

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, tree);
    }

    @Override
    public void afterHandle() {}

    @Override
    public boolean accept(File file) {
        return file.getName().endsWith(".java");
    }
}
