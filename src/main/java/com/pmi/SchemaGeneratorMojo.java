package com.pmi;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Resource;
import org.eclipse.sisu.Parameters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import com.pmi.data.Schema;
import com.pmi.data.Type;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "generate-schema", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class SchemaGeneratorMojo extends AbstractMojo {
    /**
     * Location of the main schema file.
     */
    @Parameter(defaultValue = "${project.build.resources[0].directory}", required = true)
    private File resourceDir;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    public void execute() throws MojoExecutionException {
        Schema schema = readSchema(resourceDir);
        String schemaDirPath = resourceDir.getPath() + "/schema";
        List<Type> types = readTypes(schemaDirPath, schema);
        getLog().info("Have successfully read following types: "
                + Arrays.toString(types.stream().map(t -> t.getName()).toArray()));

    }

    private Schema readSchema(File resourceDir) throws MojoExecutionException {
        String mainSchemaPath = resourceDir.getPath() + "/schema/main.yml";
        return readYaml(mainSchemaPath, new TypeReference<Schema>() {
        });
    }

    private List<Type> readTypes(String schemaDirPath, Schema schema) throws MojoExecutionException {
        Path typeFilePath = Paths.get(schemaDirPath, schema.getTypeFile());
        return readYaml(typeFilePath.toString(), new TypeReference<List<Type>>() {
        });
    }

    private <T> T readYaml(String filePath, TypeReference<T> ref) throws MojoExecutionException {
        File yamlFile = new File(filePath);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        T result = null;

        try {
            result = mapper.readValue(yamlFile, ref);
        } catch (IOException e) {
            getLog().error(e.toString());
        }

        if (result == null) {
            throw new MojoExecutionException("Failed to parse the yaml file: " + filePath.toString());
        }

        return result;
    }
}
