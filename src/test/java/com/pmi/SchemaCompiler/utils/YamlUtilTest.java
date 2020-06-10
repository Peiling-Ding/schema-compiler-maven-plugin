package com.pmi.SchemaCompiler.utils;

import static org.junit.Assert.*;

import com.pmi.SchemaCompiler.data.Schema;
import org.apache.maven.plugin.testing.WithoutMojo;
import org.junit.Test;

public class YamlUtilTest {
  @WithoutMojo
  @Test
  public void testReadSchema() throws Exception {
    String schemaDir = "./target/test-classes/schema/";
    Schema schema = YamlUtil.readSchema(schemaDir);
    assertTrue(schema != null);
    assertEquals(schema.getTypeFile(), "type.yml");
    assertEquals(schema.getEnumFile(), "enum.yml");
  }
}
