package com.pmi.SchemaCompiler;

import com.pmi.SchemaCompiler.data.Enum;
import com.pmi.SchemaCompiler.data.Schema;
import com.pmi.SchemaCompiler.data.Type;
import com.pmi.SchemaCompiler.utils.YamlUtil;
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
  @Parameter(defaultValue = "${project.build.sourceDirectory}", required = true)
  private File sourceDir;

  // The directory which should contain main.yml inside it.
  @Parameter(property = "schemaDir", required = true)
  private String schemaDir;

  public void execute() throws MojoExecutionException {
    try {
      Schema schema = YamlUtil.readSchema(schemaDir);
      List<Type> types = YamlUtil.readTypes(schemaDir, schema);
      List<Enum> enums = YamlUtil.readEnums(schemaDir, schema);
      Path packageDirPath = mkdirs();
      genTypes(packageDirPath, types);
      genEnums(packageDirPath, enums);
      genOthers(packageDirPath);
    } catch (Exception e) {
      getLog().error(e);
      throw new MojoExecutionException(e.getMessage(), e);
    }
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

  private void genOthers(Path packageDirPath) throws IOException {
    String[] files = new String[] {"IndexableEnum", "IntegerEnumSerializer"};
    for (String file : files) {
      Template template = readTemplate(file + ".mustache");
      String content = template.execute(new Object());
      writeSourceFile(packageDirPath, file, content);
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
