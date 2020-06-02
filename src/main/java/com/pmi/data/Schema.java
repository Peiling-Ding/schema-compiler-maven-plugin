package com.pmi.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Schema {
    private String typeFile;
    private String enumFile;

    public Schema(String typeFile, String enumFile) {
        this.typeFile = typeFile;
        this.enumFile = enumFile;
    }

    public Schema() {
    }

}