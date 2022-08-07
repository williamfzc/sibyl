package com.williamfzc.sibyl.core.listener.golang.base;

import com.williamfzc.sibyl.core.listener.GoParserBaseListener;
import com.williamfzc.sibyl.core.listener.base.IStorableListener;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.utils.SibylLog;
import java.io.File;

public abstract class GoStorableListener<T> extends GoParserBaseListener
        implements IStorableListener<T> {
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
            // this copy looks like handling multi threads access
            // but i am not very sure
            GoStorableListener<T> listenerCopy = this.getClass().getConstructor().newInstance();
            listenerCopy.setStorage(storage);
            listenerCopy.realHandle(file, content);
            listenerCopy.afterHandle();
        } catch (Exception e) {
            SibylLog.error(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public abstract void realHandle(File file, String content);

    @Override
    public boolean accept(File file) {
        return file.getName().endsWith(".go");
    }
}
