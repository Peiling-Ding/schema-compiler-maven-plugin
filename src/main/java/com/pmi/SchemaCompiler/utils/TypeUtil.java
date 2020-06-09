package com.pmi.SchemaCompiler.utils;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeUtil {
  private static String regx = "\\[(.*)\\]";

  public static String parseGenericType(String type) {
    Pattern pattern = Pattern.compile(regx);
    Matcher matcher = pattern.matcher(type);

    if (!matcher.matches()) {
      return parseType(type);
    } else {
      String rest = matcher.group(1);
      String result = parseGenericType(rest);
      return MessageFormat.format("List<{0}>", result);
    }
  }

  public static String parseType(String type) {
    switch (type) {
      case "string":
        return "String";
      case "integer":
        return "Integer";
      case "Int":
        return "int";
      default:
        return type;
    }
  }

  public static String getterName(String fieldName, String fieldType) {
    String firstChar = fieldName.substring(0, 1);
    String restChars = fieldName.substring(1);
    if (fieldType.equals("boolean")) {
      return MessageFormat.format("is{0}{1}", firstChar.toUpperCase(), restChars);
    } else {
      return MessageFormat.format("get{0}{1}", firstChar.toUpperCase(), restChars);
    }
  }
}
