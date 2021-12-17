# sibyl

> code analyzer for multi languages based on antlr4

## what is it

simply execute:

```java
Sibyl.genCallGraphFromDir(
        new File("src"), 
        new File("callgraph.json"), 
        SibylLangType.JAVA_8);
```

output json:

```json
[
  {
    "source": {
      "id": null,
      "versionId": null,
      "info": {
        "name": "genJava8CallGraphFromDir",
        "signature": "FileinputDir,FileoutputFile",
        "returnType": "void"
      },
      "belongsTo": {
        "clazz": {
          "name": "TestAPI",
          "packageName": "com.williamfzc.sibyl.core",
          "superName": null,
          "interfaces": null,
          "fullName": "com.williamfzc.sibyl.core.TestAPI"
        },
        "file": {
          "file": "src\\main\\java\\com\\williamfzc\\sibyl\\core\\api\\Sibyl.java",
          "startLine": 68,
          "endLine": 109
        }
      }
    },
    "target": {
      "id": null,
      "versionId": null,
      "info": {
        "name": "analyze",
        "signature": "Storage<Edge>storage",
        "returnType": "Result<Edge>"
      },
      "belongsTo": {
        "clazz": {
          "name": "EdgeAnalyzer",
          "packageName": "com.williamfzc.sibyl.core.analyzer",
          "superName": "BaseAnalyzer<Edge>",
          "interfaces": null,
          "fullName": "com.williamfzc.sibyl.core.analyzer.EdgeAnalyzer"
        },
        "file": {
          "file": "src\\main\\java\\com\\williamfzc\\sibyl\\core\\analyzer\\EdgeAnalyzer.java",
          "startLine": 27,
          "endLine": 39
        }
      }
    },
    "rawEdge": {
      "fromMethodName": "genJava8CallGraphFromDir",
      "callerType": "EdgeAnalyzer",
      "toMethodName": "analyze",
      "type": "INVOKE",
      "statement": "analyzer.analyze(edgeStorage)",
      "line": 93
    }
  },
  {
    // ...
  }
]
```

Please see [cases](./sibyl-core/src/test/java/com/williamfzc/sibyl/core/TestAPI.java) for details.

## usage

[![](https://jitpack.io/v/williamfzc/sibyl.svg)](https://jitpack.io/#williamfzc/sibyl)

### maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

dep:

```xml
<dependency>
    <groupId>com.github.williamfzc</groupId>
    <artifactId>sibyl</artifactId>
    <version>v0.1.2</version>
</dependency>
```

## licence

[Apache License 2.0](LICENSE)
