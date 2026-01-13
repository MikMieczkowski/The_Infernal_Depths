package com.mikm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.util.*;

public class YamlCopyResolver {
    private static final Yaml yaml = new Yaml();

    @SuppressWarnings("unchecked")
    private static Object resolveCopies(Object node, Object parent, FileHandle baseDir, Set<Object> visited) {
        if (node == null) return null;

        if (!visited.add(node))
            throw new RuntimeException("Detected COPY cycle in YAML structure");

        try {
            if (node instanceof Map) {
                Map<String, Object> original = (Map<String, Object>) node;

                // Work on a mutable copy so we can apply multiple COPY_* directives sequentially
                Map<String, Object> working = new LinkedHashMap<String, Object>(original);

                while (true) {
                    String copyKey = null;
                    for (String k : working.keySet()) {
                        if (k.startsWith("COPY")) { copyKey = k; break; }
                    }
                    if (copyKey == null) break;

                    boolean isSubCopy = copyKey.startsWith("COPY_") && !copyKey.equals("COPY");
                    String targetField = isSubCopy ? copyKey.substring("COPY_".length()) : null;

                    Object copyVal = working.get(copyKey);
                    Map<String, Object> copySource;

                    if (copyVal instanceof String) {
                        String s = (String) copyVal;
                        if (s.endsWith(".yaml")) {
                            FileHandle other = baseDir.child(s);
                            if (!other.exists()) throw new RuntimeException("Missing COPY file: " + s);
                            Map<String, Object> loaded = yaml.load(other.reader());
                            copySource = (Map<String, Object>) resolveCopies(loaded, loaded, other.parent(), visited);
                        } else {
                            if (!(parent instanceof Map)) throw new RuntimeException(copyKey + " by name used outside map context");
                            Map<String, Object> parentMap = (Map<String, Object>) parent;
                            if (!(parentMap.get(s) instanceof Map)) throw new RuntimeException(copyKey + " target not found: " + s);
                            copySource = (Map<String, Object>) parentMap.get(s);
                        }
                    } else if (copyVal instanceof Integer) {
                        int idx = (Integer) copyVal;
                        if (!(parent instanceof List)) throw new RuntimeException(copyKey + " by index used outside list context");
                        List<Object> list = (List<Object>) parent;
                        if (idx < 0 || idx >= list.size()) throw new RuntimeException(copyKey + " index out of bounds: " + idx);
                        copySource = (Map<String, Object>) list.get(idx);
                    } else {
                        throw new RuntimeException("Invalid " + copyKey + " value: " + copyVal);
                    }

                    Object sourceSection = copySource;
                    Map<String, Object> resolvedSource;
                    if (isSubCopy) {
                        // Resolve the entire copySource first to allow chained COPY_* within the source,
                        // then extract the requested sub-field (e.g., ANIMATION)
                        Map<String, Object> resolvedCopySource = (Map<String, Object>) resolveCopies(copySource, parent, baseDir, visited);
                        Object sub = resolvedCopySource.get(targetField);
                        if (!(sub instanceof Map)) {
                            throw new RuntimeException("COPY target missing field " + targetField);
                        }
                        sourceSection = sub;
                        resolvedSource = (Map<String, Object>) sourceSection; // already resolved above
                    } else {
                        resolvedSource = (Map<String, Object>) resolveCopies(sourceSection, parent, baseDir, visited);
                    }

                    Map<String, Object> merged;
                    if (isSubCopy) {
                        merged = new LinkedHashMap<String, Object>();
                        merged.putAll(working);
                        Map<String, Object> localSection = (Map<String, Object>) working.get(targetField);
                        if (localSection == null) localSection = new LinkedHashMap<String, Object>();
                        Map<String, Object> combined = new LinkedHashMap<String, Object>(resolvedSource);
                        combined.putAll(localSection);
                        merged.put(targetField, combined);
                        // Remove the COPY_* key to avoid reprocessing in the loop
                        merged.remove(copyKey);
                    } else {
                        merged = new LinkedHashMap<String, Object>(resolvedSource);
                        for (Map.Entry<String, Object> e : working.entrySet()) {
                            if (!copyKey.equals(e.getKey())) {
                                Object v = e.getValue();
                                if (v instanceof Map || v instanceof List)
                                    merged.put(e.getKey(), resolveCopies(v, working, baseDir, visited));
                                else
                                    merged.put(e.getKey(), v);
                            }
                        }
                    }

                    // Continue resolving; allow subsequent COPY_* keys to be applied too
                    working = merged;
                }

                // After applying all COPY directives, resolve children
                Map<String, Object> out = new LinkedHashMap<String, Object>();
                for (Map.Entry<String, Object> e : working.entrySet())
                    out.put(e.getKey(), resolveCopies(e.getValue(), working, baseDir, visited));
                return out;

            } else if (node instanceof List) {
                List<Object> list = (List<Object>) node;
                List<Object> out = new ArrayList<Object>();
                for (Object o : list)
                    out.add(resolveCopies(o, list, baseDir, visited));
                return out;
            }

            return node;
        } finally {
            visited.remove(node);
        }
    }

    /** Loads a YAML file, resolves all COPY directives, and maps the result into a POJO. */
    @SuppressWarnings("unchecked")
    public static <T> T loadAndResolve(String filePath, Class<T> type) {
        FileHandle file = Gdx.files.internal(filePath);
        Map<String, Object> raw = yaml.load(file.reader());
        Map<String, Object> resolved = (Map<String, Object>)
                resolveCopies(raw, raw, file.parent(), new HashSet<Object>());

        Map<String, Object> cleaned = (Map<String, Object>) removeCopyKeys(resolved);
        StringWriter writer = new StringWriter();
        yaml.dump(cleaned, writer);
        return yaml.loadAs(writer.toString(), type);

    }

    @SuppressWarnings("unchecked")
    private static Object removeCopyKeys(Object node) {
        if (node instanceof Map) {
            Map<String, Object> in = (Map<String, Object>) node;
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : in.entrySet()) {
                String key = e.getKey();
                if (key.startsWith("COPY")) continue; // remove COPY, COPY_CONFIG, COPY_xxx
                out.put(key, removeCopyKeys(e.getValue()));
            }
            return out;
        } else if (node instanceof List) {
            List<Object> out = new ArrayList<>();
            for (Object o : (List<Object>) node) out.add(removeCopyKeys(o));
            return out;
        }
        return node;
    }

}
