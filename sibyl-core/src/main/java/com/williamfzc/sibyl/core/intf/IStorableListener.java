package com.williamfzc.sibyl.core.intf;

import java.io.File;

public interface IStorableListener<T> extends Storable<T>, Listenable {
    @Override
    default boolean accept(File file) {
        // allow all the files
        return true;
    }

    @Override
    default void afterHandle() {}
}
