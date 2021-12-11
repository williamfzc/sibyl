package com.williamfzc.sibyl.core.listener.kt;

import com.williamfzc.sibyl.core.intf.IStorableListener;
import com.williamfzc.sibyl.core.listener.*;
import com.williamfzc.sibyl.core.storage.Storage;
import java.io.File;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

class KtStorableListener<T> extends KotlinParserBaseListener implements IStorableListener<T> {
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

    @Override
    public void handle(File file, String content) {
        curFile = file;
        KotlinLexer lexer = new KotlinLexer(CharStreams.fromString(content));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        KotlinParser parser = new KotlinParser(tokens);
        ParseTree tree = parser.kotlinFile();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, tree);
    }

    @Override
    public void afterHandle() {}

    @Override
    public boolean accept(File file) {
        return file.getName().endsWith(".kt");
    }
}
