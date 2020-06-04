package com.pmi.SchemaCompiler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pmi.SchemaCompiler.data.Enum;
import com.pmi.SchemaCompiler.data.Schema;
import com.pmi.SchemaCompiler.data.Type;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/** Goal which generate source files of data modle based on the definiation in schema yaml files. */
@Mojo(name = "generate-schema", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class SchemaGeneratorMojo extends AbstractMojo {
  // The resource directory which should contain a schema folder having main.yml inside it.
  @Parameter(defaultValue = "${project.build.resources[0].directory}", required = true)
  private File resourceDir;

  @Parameter(defaultValue = "${project.build.sourceDirectory}", required = true)
  private File sourceDir;

  public void execute() throws MojoExecutionException {
    try {
      Path schemaDirPath = Paths.get(resourceDir.getPath(), "schema");
      Schema schema = readSchema(resourceDir);
      List<Type> types = readTypes(schemaDirPath, schema);
      List<Enum> enums = readEnums(schemaDirPath, schema);
      Path packageDirPath = mkdirs();
      genTypes(packageDirPath, types);
      genEnums(packageDirPath, enums);
    } catch (Exception e) {
      getLog().error(e);
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  private Schema readSchema(File resourceDir) {
    Path mainSchemaPath = Paths.get(resourceDir.getPath(), "schema", "main.yml");
    return readYaml(mainSchemaPath.toString(), new TypeReference<Schema>() {});
  }

  private List<Type> readTypes(Path schemaDirPath, Schema schema) {
    Path typeFilePath = Paths.get(schemaDirPath.toString(), schema.getTypeFile());
    return readYaml(typeFilePath.toString(), new TypeReference<List<Type>>() {});
  }

  private List<Enum> readEnums(Path schemaDirPath, Schema schema) {
    Path enumFilePath = Paths.get(schemaDirPath.toString(), schema.getEnumFile());
    return readYaml(enumFilePath.toString(), new TypeReference<List<Enum>>() {});
  }

  private <T> T readYaml(String filePath, TypeReference<T> ref) {
    File yamlFile = new File(filePath);
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    T result = null;

    try {
      result = mapper.readValue(yamlFile, ref);
    } catch (IOException e) {
      getLog().error(e.toString());
    }

    return result;
  }

  private void genTypes(Path packageDirPath, List<Type> types) throws IOException {
    Template template = readTemplate("type.mustache");

    for (Type type : types) {
      String content = template.execute(type);
      writeSourceFile(packageDirPath, type.getName(), content);
    }
  }

  private void genEnums(Path packageDirPath, List<Enum> enums) throws IOException {
    Template template = readTemplate("enum.mustache");

    for (Enum e : enums) {
      String content = template.execute(e);
      writeSourceFile(packageDirPath, e.getName(), content);
    }
  }

  private Template readTemplate(String templateFileName) {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templateFileName);
    Reader templateReader = new InputStreamReader(inputStream);
    return Mustache.compiler().compile(templateReader);
  }

  private void writeSourceFile(Path packageDirPath, String fileName, String content)
      throws IOException {
    Path sourceFilePath = Paths.get(packageDirPath.toString(), fileName + ".java");
    Files.write(
        sourceFilePath,
        content.getBytes(),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);
  }

  private Path mkdirs() throws IOException {
    Path packageDirPath = Paths.get(sourceDir.getPath(), "com", "pmi", "data");

    if (!Files.exists(packageDirPath)) {
      Files.createDirectories(packageDirPath);
    }

    return packageDirPath;
  }
}