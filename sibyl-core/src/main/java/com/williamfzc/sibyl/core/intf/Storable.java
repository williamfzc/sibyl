package com.williamfzc.sibyl.core.intf;

import com.williamfzc.sibyl.core.storage.Storage;

// will collect data and save it to storage
public interface Storable<T> {
    void setStorage(Storage<? extends T> storage);

    Storage<? extends T> getStorage();
}
