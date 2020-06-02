package com.pmi.data;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.pmi.data.TypeFiled;

@Getter
@Setter
public class Type {
    private String name;
    private String doc;
    private List<TypeFiled> fields;
}