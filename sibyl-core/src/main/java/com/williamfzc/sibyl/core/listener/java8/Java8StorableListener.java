package com.williamfzc.sibyl.core.listener.java8;

import com.williamfzc.sibyl.core.listener.Java8BaseListener;
import com.williamfzc.sibyl.core.listener.Java8Lexer;
import com.williamfzc.sibyl.core.listener.Java8Parser;
import com.williamfzc.sibyl.core.listener.base.IStorableListener;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.utils.SibylLog;
import java.io.File;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
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

    @Override
    public void handle(File file, String content) {
        try {
            // todo: how to handle this T?
            Java8StorableListener<T> listenerCopy = this.getClass().getConstructor().newInstance();
            listenerCopy.setStorage(storage);
            listenerCopy.realHandle(file, content);
            listenerCopy.afterHandle();
        } catch (Exception e) {
            SibylLog.error(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void realHandle(File file, String content) {
        curFile = file;
        new ParseTreeWalker()
                .walk(
                        this,
                        new Java8Parser(
                                        new CommonTokenStream(
                                                new Java8Lexer(CharStreams.fromString(content))))
                                .compilationUnit());
    }

    @Override
    public boolean accept(File file) {
        return file.getName().endsWith(".java");
    }
}
