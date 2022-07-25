# sibyl

> 基于antlr4的静态代码分析库

## status

[![Dev CI](https://github.com/williamfzc/sibyl/actions/workflows/normal.yml/badge.svg)](https://github.com/williamfzc/sibyl/actions/workflows/normal.yml)
[![Tag CI](https://github.com/williamfzc/sibyl/actions/workflows/perf.yml/badge.svg)](https://github.com/williamfzc/sibyl/actions/workflows/perf.yml)
[![](https://jitpack.io/v/williamfzc/sibyl.svg)](https://jitpack.io/#williamfzc/sibyl)

## usage

用于快速为代码仓库生成逻辑快照，作为底层工具支持上层的建设开展。诸如智能diff分析、代码生成等。

### api

#### snapshot

```java
File src = new File("YOUR_PROJECT_PATH");
Snapshot snapshot = Sibyl.genSnapshotFromDir(src, SibylLangType.JAVA_8);

// get a method set
snapshot.exportFile("your_file.json");
```

```json
[{
  "info": {
    "name": "exitUnaryExpressionNotPlusMinus",
    "params": null,
    "returnType": "void"
  },
  "belongsTo": {
    "clazz": {
      "name": "Java8BaseListener",
      "superName": null,
      "interfaces": ["Java8Listener"],
      "belongsTo": {
        "pkg": {
          "name": "com.williamfzc.sibyl.core.listener"
        },
        "file": {
          "name": "sibyl-core/target/generated-sources/antlr4/com/williamfzc/sibyl/core/listener/Java8BaseListener.java",
          "startLine": 13,
          "endLine": 2883
        }
      },
      "fullName": "com.williamfzc.sibyl.core.listener.Java8BaseListener"
    },
    "file": {
      "name": "sibyl-core/target/generated-sources/antlr4/com/williamfzc/sibyl/core/listener/Java8BaseListener.java",
      "startLine": 2785,
      "endLine": 2785
    }
  },
  "lineCount": 1,
  "lineRange": [2785]
}, {
  ...
}
```

#### further ...

也提供了大量的API支持，当前优先支持java：

- All the methods you need can be found in [Sibyl.java](https://github.com/williamfzc/sibyl/blob/main/sibyl-core/src/main/java/com/williamfzc/sibyl/core/api/Sibyl.java).
- About how to use them: [TestAPI.java](https://github.com/williamfzc/sibyl/blob/main/sibyl-core/src/test/java/com/williamfzc/sibyl/core/TestAPI.java).

#### maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

dep:

[![](https://jitpack.io/v/williamfzc/sibyl.svg)](https://jitpack.io/#williamfzc/sibyl)

```xml
<dependency>
    <groupId>com.github.williamfzc</groupId>
    <artifactId>sibyl</artifactId>
    <version>USE_BADGE_VERSION_ABOVE</version>
</dependency>
```

## licence

[Apache License 2.0](LICENSE)
