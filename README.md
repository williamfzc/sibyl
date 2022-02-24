# sibyl

> code analyzer for multi languages based on antlr4

## status

[![Dev CI](https://github.com/williamfzc/sibyl/actions/workflows/normal.yml/badge.svg)](https://github.com/williamfzc/sibyl/actions/workflows/normal.yml)
[![Tag CI](https://github.com/williamfzc/sibyl/actions/workflows/perf.yml/badge.svg)](https://github.com/williamfzc/sibyl/actions/workflows/perf.yml)

## usage

### api

#### in your app

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

```xml
<dependency>
    <groupId>com.github.williamfzc</groupId>
    <artifactId>sibyl</artifactId>
    <version>v0.2.2</version>
</dependency>
```

[![](https://jitpack.io/v/williamfzc/sibyl.svg)](https://jitpack.io/#williamfzc/sibyl)

### cmd

go to your git repo and run:

```bash
curl -o- https://raw.githubusercontent.com/williamfzc/sibyl/main/scripts/diff.sh | bash
```

## licence

[Apache License 2.0](LICENSE)
