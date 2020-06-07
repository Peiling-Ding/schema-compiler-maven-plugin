package com.pmi.SchemaCompiler.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pmi.SchemaCompiler.data.Enum;
import com.pmi.SchemaCompiler.data.Schema;
import com.pmi.SchemaCompiler.data.Type;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;

public class YamlUtil {

  public static Schema readSchema(File resourceDir) throws Exception {
    Path mainSchemaPath = Paths.get(resourceDir.getPath(), "schema", "main.yml");
    return readYaml(mainSchemaPath.toString(), new TypeReference<Schema>() {});
  }

  public static List<Type> readTypes(Path schemaDirPath, Schema schema) throws Exception {
    Path typeFilePath = Paths.get(schemaDirPath.toString(), schema.getTypeFile());
    return readYaml(typeFilePath.toString(), new TypeReference<List<Type>>() {});
  }

  public static List<Enum> readEnums(Path schemaDirPath, Schema schema) throws Exception {
    Path enumFilePath = Paths.get(schemaDirPath.toString(), schema.getEnumFile());
    return readYaml(enumFilePath.toString(), new TypeReference<List<Enum>>() {});
  }

  private static <T> T readYaml(String filePath, TypeReference<T> ref) throws Exception {
    File yamlFile = new File(filePath);
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    T result = mapper.readValue(yamlFile, ref);
    if (result == null) {
      throw new Exception(MessageFormat.format("Failed to parse yaml file {0}", filePath));
    }

    return result;
  }
}
