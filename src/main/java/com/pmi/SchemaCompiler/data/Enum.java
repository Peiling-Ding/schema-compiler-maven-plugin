package com.pmi.SchemaCompiler.data;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Enum {
  private String name;
  private String doc;
  private List<EnumMember> members;
}
