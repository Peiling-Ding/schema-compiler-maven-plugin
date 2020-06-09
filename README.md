# Schema Compiler Maven Plugin

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
        <dataDir>./config</dataDir>
        <targetClass>com.pmi.data.Ad</targetClass>
    </configuration>
</plugin>
```

If you want to build and install the plugin into local maven repo, then
```bash
make build
```


## Generate Java source code for schema

## Validate the JSON data against the schema

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