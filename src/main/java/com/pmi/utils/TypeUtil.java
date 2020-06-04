package com.pmi.utils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.MessageFormat;

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
        if ("string".equals(type.toLowerCase())) {
            return "String";
        } else {
            return type;
        }
    }
}