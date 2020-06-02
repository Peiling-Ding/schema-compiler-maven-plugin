package com.pmi.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TypeFiled {
    private String name;
    private String type;
    private String meta;
    private String doc;

    public TypeFiled(String name, String type, String meta, String doc) {
        this.name = name;
        this.type = type;
        this.meta = meta;
        this.doc = doc;
    }

    public TypeFiled() {
    }
}