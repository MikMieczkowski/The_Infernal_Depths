import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.util.*;

public class PojoGenerator {
    public static void main(String[] args) throws Exception {
        new PojoGenerator().generate("entity_template.yaml", "EntityData", "com.mikm.entityLoader.generated");
    }

    public void generate(String yamlPath, String rootClass, String packageName) throws Exception {
        Map<String, Object> data = new Yaml().load(new FileInputStream(yamlPath));

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
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
                sb.append(indent).append("public ").append(className).append(" ").append(key).append(" = new ").append(className).append("();\n");
                sb.append(indent).append("public static class ").append(className).append(" {\n");
                generateClassBody(sb, (Map<String, Object>) val, indent + "    ");
                sb.append(indent).append("}\n");
            } else {
                sb.append(indent).append("public ").append(javaType(val)).append(" ").append(key).append(" = ").append(javaLiteral(val)).append(";\n");
            }
        }
    }

    private String toClassName(String key) {
        return Character.toUpperCase(key.charAt(0)) + key.substring(1);
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
