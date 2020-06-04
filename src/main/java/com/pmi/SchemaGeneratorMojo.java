package com.pmi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pmi.data.Schema;
import com.pmi.data.Type;
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
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/** Goal which generate source files of data modle based on the definiation in schema yaml files. */
@Mojo(name = "generate-schema", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class SchemaGeneratorMojo extends AbstractMojo {
  // Location of the main schema file.
  @Parameter(defaultValue = "${project.build.resources[0].directory}", required = true)
  private File resourceDir;

  @Parameter(defaultValue = "${project.build.sourceDirectory}", required = true)
  private File sourceDir;

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  public void execute() throws MojoExecutionException {
    try {
      Schema schema = readSchema(resourceDir);
      Path schemaDirPath = Paths.get(resourceDir.getPath(), "schema");
      List<Type> types = readTypes(schemaDirPath, schema);
      getLog()
          .info(
              "Have successfully read following types: "
                  + Arrays.toString(types.stream().map(t -> t.getName()).toArray()));
      Path packageDirPath = mkdirs();
      genTypes(packageDirPath, types);
    } catch (Exception e) {
      getLog().error(e);
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  private void genTypes(Path packageDirPath, List<Type> types) throws IOException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("type.mustache");
    Reader templateReader = new InputStreamReader(inputStream);
    Template template = Mustache.compiler().compile(templateReader);

    for (Type type : types) {
      String content = template.execute(type);
      Path sourceFilePath = Paths.get(packageDirPath.toString(), type.getName() + ".java");
      Files.write(sourceFilePath, content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
    }
  }

  private Schema readSchema(File resourceDir) throws Exception {
    Path mainSchemaPath = Paths.get(resourceDir.getPath(), "schema", "main.yml");
    return readYaml(mainSchemaPath.toString(), new TypeReference<Schema>() {});
  }

  private List<Type> readTypes(Path schemaDirPath, Schema schema) throws Exception {
    Path typeFilePath = Paths.get(schemaDirPath.toString(), schema.getTypeFile());
    return readYaml(typeFilePath.toString(), new TypeReference<List<Type>>() {});
  }

  private <T> T readYaml(String filePath, TypeReference<T> ref) throws Exception {
    File yamlFile = new File(filePath);
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    T result = null;

    try {
      result = mapper.readValue(yamlFile, ref);
    } catch (IOException e) {
      getLog().error(e.toString());
    }

    if (result == null) {
      throw new Exception(MessageFormat.format("Failed to parse yaml file: {0}", filePath));
    }

    return result;
  }

  private Path mkdirs() throws IOException {
    Path packageDirPath = Paths.get(sourceDir.getPath(), "com", "pmi", "data");

    if (!Files.exists(packageDirPath)) {
      Files.createDirectories(packageDirPath);
    }

    return packageDirPath;
  }
}
