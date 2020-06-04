package com.pmi.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnumMember {
  private String key;
  private String value;

  public void setKey(String key) {
    this.key = key.toUpperCase();
  }
}
