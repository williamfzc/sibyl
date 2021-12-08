package com.williamfzc.sibyl.core.intf;

import java.io.File;

// can be used by scanner
public interface Listenable {
    void handle(File file, String content);

    boolean accept(File file);
}
