package com.williamfzc.sibyl.utgen;

import com.williamfzc.sibyl.test.Support;
import java.io.IOException;
import org.junit.Test;

public class TestApi {
    @Test
    public void ok() throws IOException, InterruptedException {
        UtGen.gen(Support.getProjectRoot());
    }
}
