package com.pmi.data;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Type {
  private String name;
  private String doc;
  private List<TypeFiled> fields;
}
