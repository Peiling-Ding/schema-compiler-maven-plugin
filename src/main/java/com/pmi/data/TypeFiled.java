package com.pmi.data;

import com.pmi.utils.TypeUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TypeFiled {
  private String name;
  private String alias;
  private String type;
  private String meta;
  private String doc;

  public void setType(String type) {
    this.type = TypeUtil.parseGenericType(type);
  }
}
