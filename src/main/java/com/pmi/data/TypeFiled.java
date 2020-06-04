package com.pmi.data;

import lombok.Getter;
import lombok.Setter;

import com.pmi.utils.TypeUtil;

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