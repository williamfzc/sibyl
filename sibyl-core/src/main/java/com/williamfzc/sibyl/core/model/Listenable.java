package com.williamfzc.sibyl.core.model;

import java.io.File;

public interface Listenable {
    void handleContent(String content);

    boolean accept(File file);
}
