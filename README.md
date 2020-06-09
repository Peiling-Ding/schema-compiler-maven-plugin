# Schema Compiler Maven Plugin

Background: We have some data stored in JSON format. As all we know, JSON is felxibale and human friendly, but is very easily to be modifed into an invalid status (such as incorect JSON syntax, type mismatch and so on) by human. These data is very curcial in production environment and impacts revenue a lot. So we want to caputre any possible error when someone is making any modification to these data. 

Solution: Use a domain specific langue (DSL) as the single source of truth. It describes the schema and validates the JSON data against the schema in build time. This plugin is the compiler of this DSL. Jusk like compilers of other programing languages, it takes the DSL as input and generates something as output. Currtenly the compiler does following things:
    1. Generates the Java source code for the schema
    2. Generates the document.
    3. Validate the JSON data against the schema.
 
If any error is captured during the compiling process, we fail the CI which blocks the PR merging. In this way we guard the repo and make sure every modification is valid.

## How to use

Add following dependency to your `pom.xml`

```xml
<plugin>
    <groupId>com.pmi</groupId>
    <artifactId>schema-compiler-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <executions>
        <execution>
            <id>generate-schema</id>
            <goals>
                <goal>generate-schema</goal>
            </goals>
        </execution>
        <execution>
            <id>validate-schema</id>
            <goals>
                <goal>validate-schema</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <schemaDir>src/main/resouces/schema</schemaDir>
        <dataDir>config</dataDir>
        <targetClass>com.pmi.data.Ad</targetClass>
    </configuration>
</plugin>
```

If you want to build and install the plugin into local maven repo, then
```bash
make build
```

Create a `main.yml` file under the `schemaDir` dir and specify the schema definition files inside it. For example, you can create the `main.yml` file with following content:

```yml
---
typeFile: type.yml
enumFile: enum.yml
```

## The DSL

## Golas of this plugin

This plugin has two golas, one is to generate the source code of the schema and the other one is to validate the JSON data matches the schema. If you don't need to do validation, you can use the first goal alone.

### Generate Java source code for schema

### Validate the JSON data against the schema

Following cheks will be done:

1. JSON syntax.
2. JSON key absent if the schema is marked as `nullable=false`.
3. Duplicate keys in JSON data.
4. Following type rules
   
| Schema Type  | Accepted JSON value format | Rejected JSON value format |
| ------------ | -------------------------- | -------------------------- |
| `int` | `1` | `null`, `1.0`, `"1"` |
| `Integer` | `1`, `null` | `1.0`, `"1"` |
| `double` | `1`, `1.0` | `null`, `"1"`, `"1.0"` |
| `Double` | `1`, `1.0`, `null` | `"1"`, `"1.0"` |
| `boolean` | `true`, `false` | `null`, and others |
| `Boolean` | `true`, `false`, `null` | all other values |
| `string` | Strings surrounded by `"` | Values in other formats  |
| `enum` | Integer or string that is a member of the enum type |  |