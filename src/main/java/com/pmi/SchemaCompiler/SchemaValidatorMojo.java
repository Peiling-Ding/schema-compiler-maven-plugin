package com.pmi.SchemaCompiler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
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
  // The directory which contains all the JSON data to be validated.
  @Parameter(property = "dataDir", required = true)
  private String dataDir;

  // The target class to validate.
  @Parameter(property = "targetClass", required = true)
  private String targetClass;

  @Parameter(defaultValue = "${project}")
  private MavenProject project;

  public void execute() throws MojoExecutionException {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      Class c = getClassLoader(project).loadClass(targetClass);
      String projectBasePath = project.getBasedir().toPath().toString();
      Path dataDirPath = Paths.get(projectBasePath, dataDir);
      List<Path> paths = Files.walk(dataDirPath).collect(Collectors.toList());
      for (Path path : paths) {
        validate(objectMapper, path, c);
      }
    } catch (Exception e) {
      getLog().error(e);
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  private ClassLoader getClassLoader(MavenProject project)
      throws DependencyResolutionRequiredException, MalformedURLException {
    List<String> classpathElements = project.getCompileClasspathElements();
    classpathElements.add(project.getBuild().getOutputDirectory());
    classpathElements.add(project.getBuild().getTestOutputDirectory());

    URL urls[] = new URL[classpathElements.size()];
    for (int i = 0; i < classpathElements.size(); ++i) {
      urls[i] = new File(classpathElements.get(i).toString()).toURI().toURL();
    }

    return new URLClassLoader(urls, this.getClass().getClassLoader());
  }

  private void validate(ObjectMapper objectMapper, Path filePath, Class c)
      throws IOException, JsonParseException, JsonMappingException, MojoExecutionException {
    if (filePath.getFileName().toString().endsWith(".json")) {
      getLog().info(MessageFormat.format("Validating file: {0}", filePath.toString()));
      Object o =
          objectMapper
              .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
              .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
              .readValue(filePath.toUri().toURL(), c);
      JsonNode acutal = objectMapper.readTree(objectMapper.writeValueAsString(o));
      JsonNode expected = objectMapper.readTree(filePath.toUri().toURL());
      if (!acutal.equals(expected)) {
        throw new MojoExecutionException(
            MessageFormat.format(
                "Schema validation failed;\nSchema file: {0}\nExpected JSON: {1}\nActual JSON:{2}",
                filePath.toString(), expected, acutal));
      }
    }
  }
}
