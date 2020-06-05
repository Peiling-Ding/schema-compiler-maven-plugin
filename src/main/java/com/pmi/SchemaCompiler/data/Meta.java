package com.pmi.SchemaCompiler.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Meta {
  private boolean nullable;
  private boolean serializeEnumAsInt;
}
