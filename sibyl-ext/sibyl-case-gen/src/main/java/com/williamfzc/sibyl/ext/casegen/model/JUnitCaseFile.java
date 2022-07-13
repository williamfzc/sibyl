package com.williamfzc.sibyl.ext.casegen.model;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.williamfzc.sibyl.core.utils.SibylUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Data;

/*
for some string replacement
 */
@Data
public class JUnitCaseFile {
    private JavaFile javaFile;

    public Path writeToDir(Path directory, boolean rewrite) throws IOException {
        Path outputDirectory = directory;
        if (!javaFile.packageName.isEmpty()) {
            for (String packageComponent : javaFile.packageName.split("\\.")) {
                outputDirectory = outputDirectory.resolve(packageComponent);
            }
            Files.createDirectories(outputDirectory);
        }

        Path outputPath = outputDirectory.resolve(javaFile.typeSpec.name + ".java");
        if (outputPath.toFile().isFile() && !rewrite) {
            // will not cover
            return null;
        }
        Files.write(outputPath, javaFile.toString().getBytes(StandardCharsets.UTF_8));
        return outputPath;
    }

    public Path writeToDir(Path directory) throws IOException {
        return writeToDir(directory, false);
    }

    public String genValidCaseContent() {
        String raw = javaFile.toString();
        for (FieldSpec eachField : javaFile.typeSpec.fieldSpecs) {
            String fullType = eachField.type.toString();
            String clazzName = SibylUtils.fullPath2ClazzName(fullType);
            raw = raw.replaceAll(" " + clazzName, " " + fullType);
            break;
        }
        return raw;
    }

    public static JUnitCaseFile of(JavaFile javaFile) {
        JUnitCaseFile f = new JUnitCaseFile();
        f.setJavaFile(javaFile);
        return f;
    }
}
