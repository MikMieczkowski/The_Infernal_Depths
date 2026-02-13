package com.mikm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A preprocessor for YAML files that adds two features on top of standard YAML:
 * C-like #DEFINE text substitution and COPY-based inheritance/templating.
 *
 * Processing order: #DEFINE substitution runs first as a raw text pass,
 * then the resulting YAML is parsed and COPY directives are resolved.
 *
 * ============================================================
 * #DEFINE  (text substitution, like C preprocessor #define)
 * ============================================================
 * Must appear at the very top of the file before any YAML content.
 * Replaces all whole-word occurrences of the variable with its value.
 * A "whole word" means no adjacent letter or underscore (a-zA-Z_).
 *
 *   #DEFINE SPEED 0.5
 *   #DEFINE DMG 10
 *   USAGE_TIME: SPEED       -> becomes USAGE_TIME: 0.5
 *   DAMAGE: DMG             -> becomes DAMAGE: 10
 *   SPEED_BONUS: 2          -> NOT replaced (SPEED is part of SPEED_BONUS)
 *
 * ============================================================
 * COPY  (inheritance / templating within the YAML structure)
 * ============================================================
 * There are two forms: full COPY and sub-field COPY_*.
 *
 * --- Full COPY ---
 * Copies all fields from a source as a base, then any sibling fields in the
 * same map act as overrides. The source can be specified three ways:
 *
 * 1) By external file path (value ends in ".yaml"):
 *      COPY: copperSword.yaml
 *      COOLDOWN: 1              # overrides whatever copperSword.yaml had
 *
 * 2) By sibling name (value is a string, inside a map context):
 *      enemies:
 *        baseRat:
 *          HP: 5
 *          SPEED: 2
 *        fastRat:
 *          COPY: baseRat        # inherits HP: 5, SPEED: 2 from sibling
 *          SPEED: 4             # overrides SPEED to 4
 *
 * 3) By list index (value is an integer, inside a list context):
 *      PROJECTILES:
 *        - CREATE_ON: PRESS
 *          ANIMATION_NAME: "slice"
 *          SPEED: 0
 *        - COPY: 0              # copies from PROJECTILES[0]
 *          SPEED: 1             # overrides SPEED to 1
 *
 *    When a full COPY from a file brings in a list, and the local file also
 *    defines that same list, COPY: index inside the local list items resolves
 *    against the SOURCE file's list (not the local one). This lets you inherit
 *    a list item and selectively override fields:
 *
 *      COPY: base.yaml          # base.yaml has PROJECTILES: [{CREATE_ON: PRESS, SPEED: 0, ...}]
 *      PROJECTILES:
 *        - COPY: 0              # copies base.yaml's PROJECTILES[0]
 *          SPEED: 5             # overrides SPEED to 5
 *  CLAUDE:
 *  ❯ Is this the best way to have it, or does a different way make more sense?
 *
 *   This is explicit - you opt in to inheriting a source item with COPY: 0. But it creates an inconsistency: COPY: 0 means "source's item 0" when overriding a source list, vs "this list's item 0" in normal contexts.
 *
 *   Alternative: auto-merge lists by index. No COPY: 0 needed:
 *   COPY: swingRegular.yaml
 *   PROJECTILES:
 *     - WIDTH_MULTIPLIER: 2    # auto-merges into source's PROJECTILES[0]
 *   Simpler, but then you can't fully replace a list item or add a completely new one without inheriting from the source. Unclear semantics when list lengths differ.
 *
 *   Alternative: just write it out. No resolver change, duplicate the fields. Safe but verbose - defeats the purpose of COPY.
 *
 *   I think the current approach is the best option. It's explicit (you choose to inherit with COPY: 0), and a list item without COPY is standalone. The context-dependent behavior of COPY: 0 is a minor inconsistency, but in
 *   practice you'd only ever use it this way when overriding a source list. Does that reasoning make sense, or were you thinking of a different direction?
 *
 * --- Sub-field COPY_* ---
 * Copies only a specific sub-field from a sibling entry by name.
 * The field name is the suffix after "COPY_". Local values override.
 *
 *      map:
 *        elem1:
 *          CONFIG:
 *            data1: 1
 *            data2: 2
 *            data3: 3
 *        elem2:
 *          COPY_CONFIG: elem1   # copies only elem1's CONFIG sub-field
 *          CONFIG:
 *            data2: 7           # overrides data2; data1 and data3 inherited
 *
 * COPY directives are resolved recursively - a copied source can itself
 * contain COPY directives. Cycles are detected and throw an error.
 * Multiple COPY/COPY_* keys can coexist in the same map and are applied sequentially.
 */
public class YamlCopyResolver {
    private static final Yaml yaml;
    static {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setAllowDuplicateKeys(false);
        yaml = new Yaml(loaderOptions);
    }
    private static final Pattern DEFINE_PATTERN = Pattern.compile("^#DEFINE\\s+(\\S+)\\s+(.+)$");
    /** Loads a YAML file, applies #DEFINE and COPY resolution, and maps the result into a POJO. */
    @SuppressWarnings("unchecked")
    public static <T> T loadAndResolve(String filePath, Class<T> type) {
        FileHandle file = Gdx.files.internal(filePath);
        String preprocessed = preprocess(file.readString());
        Map<String, Object> raw = yaml.load(new StringReader(preprocessed));
        Map<String, Object> resolved = (Map<String, Object>)
                resolveCopies(raw, raw, file.parent(), new HashSet<Object>());

        Map<String, Object> cleaned = (Map<String, Object>) removeCopyKeys(resolved);
        StringWriter writer = new StringWriter();
        yaml.dump(cleaned, writer);
        return yaml.loadAs(writer.toString(), type);

    }

    // Public for testing
    public static String preprocess(String text) {
        String[] lines = text.split("\n", -1);
        Map<String, String> defines = new LinkedHashMap<>();
        int firstNonDefineLine = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#DEFINE")) {
                Matcher m = DEFINE_PATTERN.matcher(line);
                if (m.matches()) {
                    String varName = m.group(1);
                    String value = m.group(2);
                    defines.put(varName, value);
                }
                firstNonDefineLine = i + 1;
            } else {
                break;
            }
        }

        if (defines.isEmpty()) {
            return text;
        }

        StringBuilder content = new StringBuilder();
        for (int i = firstNonDefineLine; i < lines.length; i++) {
            if (i > firstNonDefineLine) {
                content.append("\n");
            }
            content.append(lines[i]);
        }

        String result = content.toString();

        for (Map.Entry<String, String> entry : defines.entrySet()) {
            String varName = entry.getKey();
            String value = entry.getValue();
            // Whole-word match: no adjacent letter or underscore
            String pattern = "(?<![a-zA-Z_])" + Pattern.quote(varName) + "(?![a-zA-Z_])";
            result = result.replaceAll(pattern, Matcher.quoteReplacement(value));
        }

        return result;
    }

    /** Recursively walks the parsed YAML tree, resolving all COPY and COPY_* directives. */
    @SuppressWarnings("unchecked")
    private static Object resolveCopies(Object node, Object parent, FileHandle baseDir, Set<Object> visited) {
        if (node == null) return null;

        if (!visited.add(node))
            throw new RuntimeException("Detected COPY cycle in YAML structure");

        try {
            if (node instanceof Map) {
                Map<String, Object> original = (Map<String, Object>) node;
                Map<String, Object> working = new LinkedHashMap<String, Object>(original);

                // Process COPY/COPY_* keys one at a time until none remain
                while (true) {
                    String copyKey = null;
                    for (String k : working.keySet()) {
                        if (k.startsWith("COPY")) { copyKey = k; break; }
                    }
                    if (copyKey == null) break;

                    boolean isSubCopy = copyKey.startsWith("COPY_") && !copyKey.equals("COPY");
                    String targetField = isSubCopy ? copyKey.substring("COPY_".length()) : null;

                    // Resolve the copy source depending on value type
                    Object copyVal = working.get(copyKey);
                    Map<String, Object> copySource;

                    if (copyVal instanceof String) {
                        String s = (String) copyVal;
                        if (s.endsWith(".yaml")) {
                            // External file reference
                            FileHandle other = baseDir.child(s);
                            if (!other.exists()) throw new RuntimeException("Missing COPY file: " + s);
                            String preprocessed = preprocess(other.readString());
                            Map<String, Object> loaded = yaml.load(new StringReader(preprocessed));
                            copySource = (Map<String, Object>) resolveCopies(loaded, loaded, other.parent(), visited);
                        } else {
                            // Sibling name reference within same parent map
                            if (!(parent instanceof Map)) throw new RuntimeException(copyKey + " by name used outside map context");
                            Map<String, Object> parentMap = (Map<String, Object>) parent;
                            if (!(parentMap.get(s) instanceof Map)) throw new RuntimeException(copyKey + " target not found: " + s);
                            copySource = (Map<String, Object>) parentMap.get(s);
                        }
                    } else if (copyVal instanceof Integer) {
                        // List index reference within same parent list
                        int idx = (Integer) copyVal;
                        if (!(parent instanceof List)) throw new RuntimeException(copyKey + " by index used outside list context");
                        List<Object> list = (List<Object>) parent;
                        if (idx < 0 || idx >= list.size()) throw new RuntimeException(copyKey + " index out of bounds: " + idx);
                        copySource = (Map<String, Object>) list.get(idx);
                    } else {
                        throw new RuntimeException("Invalid " + copyKey + " value: " + copyVal);
                    }

                    // Resolve the source, then merge with local overrides
                    Object sourceSection = copySource;
                    Map<String, Object> resolvedSource;
                    if (isSubCopy) {
                        // COPY_FIELD: resolve source fully, then extract just the named sub-field
                        Map<String, Object> resolvedCopySource = (Map<String, Object>) resolveCopies(copySource, parent, baseDir, visited);
                        Object sub = resolvedCopySource.get(targetField);
                        if (!(sub instanceof Map)) {
                            throw new RuntimeException("COPY target missing field " + targetField);
                        }
                        sourceSection = sub;
                        resolvedSource = (Map<String, Object>) sourceSection;
                    } else {
                        resolvedSource = (Map<String, Object>) resolveCopies(sourceSection, parent, baseDir, visited);
                    }

                    // Merge: source fields as base, local fields as overrides
                    Map<String, Object> merged;
                    if (isSubCopy) {
                        // Only merge into the targeted sub-field, keep everything else
                        merged = new LinkedHashMap<String, Object>();
                        merged.putAll(working);
                        Map<String, Object> localSection = (Map<String, Object>) working.get(targetField);
                        if (localSection == null) localSection = new LinkedHashMap<String, Object>();
                        Map<String, Object> combined = new LinkedHashMap<String, Object>(resolvedSource);
                        combined.putAll(localSection);
                        merged.put(targetField, combined);
                        merged.remove(copyKey);
                    } else {
                        // Full COPY: source as base, all non-COPY local fields override
                        merged = new LinkedHashMap<String, Object>(resolvedSource);
                        for (Map.Entry<String, Object> e : working.entrySet()) {
                            if (!copyKey.equals(e.getKey())) {
                                Object v = e.getValue();
                                if (v instanceof List && resolvedSource.get(e.getKey()) instanceof List) {
                                    // Local list overriding a source list: resolve COPY: index
                                    // against the SOURCE list so items can inherit from the original
                                    List<Object> sourceList = (List<Object>) resolvedSource.get(e.getKey());
                                    List<Object> localList = (List<Object>) v;
                                    List<Object> resolvedList = new ArrayList<Object>();
                                    for (Object item : localList)
                                        resolvedList.add(resolveCopies(item, sourceList, baseDir, visited));
                                    merged.put(e.getKey(), resolvedList);
                                } else if (v instanceof Map || v instanceof List)
                                    merged.put(e.getKey(), resolveCopies(v, working, baseDir, visited));
                                else
                                    merged.put(e.getKey(), v);
                            }
                        }
                    }

                    working = merged;
                }

                // Recurse into children after all COPY directives are resolved
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

    /** Strips all COPY/COPY_* keys from the resolved tree so they don't appear in the final output. */
    @SuppressWarnings("unchecked")
    private static Object removeCopyKeys(Object node) {
        if (node instanceof Map) {
            Map<String, Object> in = (Map<String, Object>) node;
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : in.entrySet()) {
                String key = e.getKey();
                if (key.startsWith("COPY")) continue;
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
