package com.mikm;

import java.util.Map;

public class StringUtils {
    public static void prettyPrint(Object obj) {
        System.out.println(prettyPrint(obj, 0));
    }

    private static String prettyPrint(Object obj, int indent) {
        StringBuilder sb = new StringBuilder();
        String ind = repeat("  ", indent);

        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            sb.append("{\n");
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                sb.append(ind).append("  ").append(entry.getKey()).append(": ");
                sb.append(prettyPrint(entry.getValue(), indent + 1));
                sb.append("\n");
            }
            sb.append(ind).append("}");
        } else if (obj instanceof Iterable) {
            Iterable<?> list = (Iterable<?>) obj;
            sb.append("[\n");
            for (Object item : list) {
                sb.append(ind).append("  ");
                sb.append(prettyPrint(item, indent + 1));
                sb.append("\n");
            }
            sb.append(ind).append("]");
        } else {
            sb.append(obj);
        }

        return sb.toString();
    }

    private static String repeat(String s, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}
