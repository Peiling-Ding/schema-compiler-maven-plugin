package com.pmi.SchemaCompiler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.pmi.SchemaCompiler.data.Field;
import com.pmi.SchemaCompiler.data.Meta;
import com.pmi.SchemaCompiler.data.Schema;
import com.pmi.SchemaCompiler.data.Type;
import com.pmi.SchemaCompiler.utils.TypeUtil;
import com.pmi.SchemaCompiler.utils.YamlUtil;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Goal which validate if the JSON data inside {@code dataDir} matches the class definition
 * specified by the {@ targetClass} .
 */
@Mojo(name = "validate-schema", defaultPhase = LifecyclePhase.VERIFY)
public class SchemaValidatorMojo extends AbstractMojo {
  @Parameter(defaultValue = "${project}")
  private MavenProject project;

  // The directory which should contain main.yml inside it.
  @Parameter(property = "schemaDir", required = true)
  private String schemaDir;

  // The directory which contains all the JSON data to be validated.
  @Parameter(property = "dataDir", required = true)
  private String dataDir;

  // The target class to validate.
  @Parameter(property = "targetClass", required = true)
  private String targetClass;

  private Map<String, Type> allTypes;

  private static Comparator<JsonNode> comparator =
      new Comparator<JsonNode>() {
        @Override
        public int compare(JsonNode o1, JsonNode o2) {
          if (o1.equals(o2)) {
            return 0;
          }
          if ((o1 instanceof NumericNode) && (o2 instanceof NumericNode)) {
            double d1 = ((NumericNode) o1).asDouble();
            double d2 = ((NumericNode) o2).asDouble();
            if (d1 == d2) {
              return 0;
            }
          }
          return 1;
        }
      };

  public void execute() throws MojoExecutionException {
    try {
      loadSchemaTypes();
      ObjectMapper objectMapper = new ObjectMapper();
      Class c = getClassLoader(project).loadClass(targetClass);
      String projectBasePath = project.getBasedir().toPath().toString();
      Path dataDirPath = Paths.get(projectBasePath, dataDir);
      List<Path> dataFilePaths = Files.walk(dataDirPath).collect(Collectors.toList());
      for (Path dataFilePath : dataFilePaths) {
        validate(objectMapper, dataFilePath, c);
      }
    } catch (Exception e) {
      getLog().error(e);
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  private ClassLoader getClassLoader(MavenProject project) throws Exception {
    List<String> classpathElements = project.getCompileClasspathElements();
    classpathElements.add(project.getBuild().getOutputDirectory());
    classpathElements.add(project.getBuild().getTestOutputDirectory());

    URL urls[] = new URL[classpathElements.size()];
    for (int i = 0; i < classpathElements.size(); ++i) {
      urls[i] = new File(classpathElements.get(i).toString()).toURI().toURL();
    }

    return new URLClassLoader(urls, this.getClass().getClassLoader());
  }

  private Type getTargetType(Class targetC) {
    return this.allTypes.get(targetC.getSimpleName());
  }

  private void loadSchemaTypes() throws Exception {
    Schema schema = YamlUtil.readSchema(schemaDir);
    List<Type> types = YamlUtil.readTypes(schemaDir, schema);
    this.allTypes = types.stream().collect(Collectors.toMap(type -> type.getName(), type -> type));
  }

  private void validate(ObjectMapper objectMapper, Path filePath, Class targetC) throws Exception {
    if (filePath.getFileName().toString().endsWith(".json")) {
      getLog().info(MessageFormat.format("Validating file: {0}", filePath.toString()));
      Object targetObject =
          objectMapper
              .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
              .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
              .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)
              .readValue(filePath.toUri().toURL(), targetC);
      JsonNode acutal = objectMapper.readTree(objectMapper.writeValueAsString(targetObject));
      JsonNode expected = objectMapper.readTree(filePath.toUri().toURL());
      if (!acutal.equals(comparator, expected)) {
        throw new Exception(
            MessageFormat.format(
                "Schema validation failed;\nSchema file: {0}\nExpected JSON: {1}\nActual JSON:{2}",
                filePath.toString(), expected, acutal));
      }

      validateTargetType(filePath, targetObject, targetC);
    }
  }

  private void validateTargetType(Path filePath, Object target, Class targetC) throws Exception {
    if (target instanceof List) {
      List list = (List) target;
      for (Object t : list) {
        validateTargetType(filePath, t, t.getClass());
      }
    }

    Type type = getTargetType(targetC);
    if (type == null) {
      return;
    }

    for (Field field : type.getFields()) {
      if (field.getMeta() != null) {
        String getterName = TypeUtil.getterName(field.getName(), field.getType());
        Method getterMethod = targetC.getMethod(getterName);
        Object fieldValue = getterMethod.invoke(target);
        validateFieldMeta(filePath, fieldValue, field);

        // Recursively validate each field.
        validateTargetType(filePath, fieldValue, fieldValue.getClass());
      }
    }
  }

  private void validateFieldMeta(Path filePath, Object fieldValue, Field field) throws Exception {
    if (!validateNullable(fieldValue, field.getMeta())) {
      throw new Exception(
          MessageFormat.format(
              "Failed to parse file {0}, reason: {1} cannot be null.",
              filePath.toString(), field.getName()));
    }
    // More validations can be added here.
  }

  private boolean validateNullable(Object fieldValue, Meta fieldMeta) {
    if (false == fieldMeta.getNullable()) {
      return fieldValue != null;
    } else {
      return true;
    }
  }
}
