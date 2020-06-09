package com.pmi.SchemaCompiler.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pmi.SchemaCompiler.data.Enum;
import com.pmi.SchemaCompiler.data.Schema;
import com.pmi.SchemaCompiler.data.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;

public class YamlUtil {

  public static Schema readSchema(String schemaDir) throws Exception {
    Path mainSchemaPath = Paths.get(schemaDir, "main.yml");
    return readYaml(mainSchemaPath, new TypeReference<Schema>() {});
  }

  public static List<Type> readTypes(String schemaDir, Schema schema) throws Exception {
    Path typeFilePath = Paths.get(schemaDir, schema.getTypeFile());
    return readYaml(typeFilePath, new TypeReference<List<Type>>() {});
  }

  public static List<Enum> readEnums(String schemaDir, Schema schema) throws Exception {
    Path enumFilePath = Paths.get(schemaDir, schema.getEnumFile());
    return readYaml(enumFilePath, new TypeReference<List<Enum>>() {});
  }

  private static <T> T readYaml(Path filePath, TypeReference<T> ref) throws Exception {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    T result = mapper.readValue(filePath.toUri().toURL(), ref);
    if (result == null) {
      throw new Exception(MessageFormat.format("Failed to parse yaml file {0}", filePath));
    }

    return result;
  }
}
