package com.mikm;

import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.util.*;

public class PojoGenerator {
    public static void main(String[] args) throws Exception {
        String assetsPath = "D:\\IntelliJprojects\\The_Infernal_Depths-master(1)\\The_Infernal_Depths-master\\assets\\";
        String packageName = "com.mikm";
        new PojoGenerator().generate(assetsPath + "yaml\\combat\\weapon.yaml", packageName);
    }

    public void generate(String yamlPath, String packageName) throws Exception {
        Map<String, Object> data = new Yaml().load(new FileInputStream(yamlPath));

        File yamlFile = new File(yamlPath);
        String fileName = yamlFile.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String rootClass = toClassName(baseName) + "Data";

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
        sb.append("import java.util.*;\n\n");
        sb.append("public class ").append(rootClass).append(" {\n");

        generateClassBody(sb, data, "    ");

        sb.append("}\n");

        File out = new File(rootClass + ".java");
        try (FileWriter fw = new FileWriter(out)) {
            fw.write(sb.toString());
        }

        System.out.println("Generated " + out.getAbsolutePath());
    }

    @SuppressWarnings("unchecked")
    private void generateClassBody(StringBuilder sb, Map<String, Object> map, String indent) {
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String key = e.getKey();
            Object val = e.getValue();

            if (val instanceof Map) {
                String className = toClassName(key);
                sb.append(indent).append("public ").append(className).append(" ").append(key)
                        .append(" = new ").append(className).append("();\n");
                sb.append(indent).append("public static class ").append(className).append(" {\n");
                generateClassBody(sb, (Map<String, Object>) val, indent + "    ");
                sb.append(indent).append("}\n");

            } else if (val instanceof List) {
                List<?> list = (List<?>) val;
                if (list.isEmpty()) {
                    sb.append(indent).append("public List<Object> ").append(key)
                            .append(" = new ArrayList<>();\n");
                } else {
                    Object first = list.get(0);
                    if (first instanceof Map) {
                        String itemClassName = toClassName(key) + "Item";
                        sb.append(indent).append("public List<").append(itemClassName).append("> ")
                                .append(key).append(" = new ArrayList<>();\n");

                        sb.append(indent).append("public static class ").append(itemClassName).append(" {\n");
                        generateClassBody(sb, (Map<String, Object>) first, indent + "    ");
                        sb.append(indent).append("}\n");
                    } else {
                        String type = javaType(first);
                        sb.append(indent).append("public List<").append(type).append("> ")
                                .append(key).append(" = new ArrayList<>(Arrays.asList(");
                        for (int i = 0; i < list.size(); i++) {
                            sb.append(javaLiteral(list.get(i)));
                            if (i < list.size() - 1) sb.append(", ");
                        }
                        sb.append("));\n");
                    }
                }

            } else {
                sb.append(indent).append("public ").append(javaType(val)).append(" ").append(key)
                        .append(" = ").append(javaLiteral(val)).append(";\n");
            }
        }
    }

    private String toClassName(String key) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : key.toCharArray()) {
            if (c == '_' || c == '-' || c == ' ') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }

        return result.toString();
    }


    private String javaType(Object val) {
        if (val instanceof Integer) return "int";
        if (val instanceof Double || val instanceof Float) return "double";
        if (val instanceof Boolean) return "boolean";
        return "String";
    }

    private String javaLiteral(Object val) {
        if (val instanceof String) return "\"" + val + "\"";
        return String.valueOf(val);
    }
}
