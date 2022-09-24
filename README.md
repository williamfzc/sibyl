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

支持多种语言：

- java（完成
- kotlin（初步可用
- golang（进行中
- ...

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

## 声明

### 2022-09-24

随着试用范围的扩大，这个项目暴露出不少设计缺陷。在此说明。

#### 性能（antlr4）

- 基于 java，在性能上比起 tree-sitter 之类的纯c库首先输一阵；
- 默认情况下使用全量的语言解析规则（which means 无论你需要与否，所有的规则都会走到），在一些简单场景下这部分性能是浪费的；
- 不支持增量分析（不是非常重要但

尽管针对java做了优化，但目前遇到大型java文件的时候依旧会轻易把机器CPU拉满。
另，antlr4十分优秀，但各类分析引擎之间其实定位并不完全一致，需要思考清楚。
#### 数据结构设计（sibyl）

暴露了我自己对编程语言的理解有限，轻率设计了第一版的snapshot树状结构+递归式的listener。树状结构非常不合理。

java方法是树状嵌套在class内的，但许多语言并不是，例如go。如此做，在迁移至其他语言时采集逻辑几乎无法复用。

应参考：

- AST层：https://github.com/github/semantic/blob/main/docs/examples.md#symbols
- 基于AST层做快照层

## licence

[Apache License 2.0](LICENSE)
