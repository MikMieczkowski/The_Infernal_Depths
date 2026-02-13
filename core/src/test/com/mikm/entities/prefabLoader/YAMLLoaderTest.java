package com.mikm.entities.prefabLoader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessFiles;
import com.badlogic.gdx.backends.headless.HeadlessNativesLoader;
import com.badlogic.gdx.files.FileHandle;
import com.mikm.entities.prefabLoader.weapon.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Test suite for YAMLLoader system.
 * Tests schema defaults, merging, transformation, auto-copy, and caching.
 */
public class YAMLLoaderTest {

    public static void main(String[] args) throws IOException {
        HeadlessNativesLoader.load();
        Gdx.files = new HeadlessFiles();

        // Create yaml/combat directory if needed
        createYamlDirectory();

        // Write test YAML files
        writeSchemaYAML();
        writeInstanceYAML();
        writeSimpleTestYAML();

        // Register transformers
        WeaponTransformers.register();

        try {
            testRawLoadingWithSchemaDefaults();
            testFormattedLoadingWithTransformation();
            testNestedObjectAutoCopy();
            testComboTreeTransformation();
            testCaching();

            System.out.println("\n✅ All YAMLLoader tests passed!");
        } finally {
            // Clean up
            YAMLLoader.clearCache();
        }
    }

    private static void createYamlDirectory() {
        FileHandle dir = Gdx.files.local("yaml/combat");
        dir.mkdirs();
    }

    /**
     * Test 1: Raw loading with schema defaults merging.
     * Instance overrides schema, missing fields use schema defaults.
     */
    private static void testRawLoadingWithSchemaDefaults() {
        System.out.println("\n--- Test 1: Raw Loading with Schema Defaults ---");

        WeaponRawData rawWeapon = YAMLLoader.load(
                "combat/testSword.yaml",
                "combat/weapon.yaml",
                WeaponRawData.class
        );

        // Verify defaults from schema
        assert rawWeapon.ORBIT != null : "ORBIT should not be null";
        assert rawWeapon.ORBIT.IMAGE_X == 0 : "IMAGE_X should default to 0 from schema";
        assert rawWeapon.ORBIT.ORBIT_DISTANCE == 15f : "ORBIT_DISTANCE should default to 15 from schema";
        assert rawWeapon.ORBIT.ORBIT_TYPE.equals("SWAP") : "ORBIT_TYPE should default to SWAP from schema";

        // Verify instance override
        assert rawWeapon.ORBIT.IMAGE_X == 0 : "IMAGE_X override not applied";

        // Verify config exists
        assert rawWeapon.CONFIG != null : "CONFIG should not be null";
        assert rawWeapon.CONFIG.containsKey("swingRegular") : "Config should have swingRegular";
        assert rawWeapon.CONFIG.get("swingRegular").PROJECTILE_DAMAGE == null : "swingRegular PROJECTILE_DAMAGE should be null from schema (no default list)";

        System.out.println("✓ Raw loading with schema defaults passed");
    }

    /**
     * Test 2: Formatted loading with COMBO_TREE transformation.
     * Transforms raw Map<String, Object> to List<AttackNode>.
     */
    private static void testFormattedLoadingWithTransformation() {
        System.out.println("\n--- Test 2: Formatted Loading with Transformation ---");

        WeaponFormattedData formattedWeapon = YAMLLoader.load(
                "combat/testSword.yaml",
                "combat/weapon.yaml",
                WeaponRawData.class,
                WeaponFormattedData.class
        );

        // Verify COMBO_TREE was transformed
        assert formattedWeapon.COMBO_TREE != null : "COMBO_TREE should not be null";
        assert formattedWeapon.COMBO_TREE.size() > 0 : "COMBO_TREE should have at least one root attack";

        // Verify first attack node
        AttackNode firstAttack = formattedWeapon.COMBO_TREE.get(0);
        assert firstAttack.duration == AttackDuration.LIGHT : "First attack should be LIGHT";
        assert firstAttack.attackName.equals("swingRegular") : "Attack name should be 'swingRegular'";

        // Verify combo depth using thenNext
        assert !firstAttack.thenNext.isEmpty() : "Light swing should have thenNext children (follow-ups)";
        AttackNode followUp = firstAttack.thenNext.get(AttackDuration.LIGHT);
        assert followUp != null : "Should have LIGHT follow-up";
        assert followUp.duration == AttackDuration.LIGHT : "Follow-up should also be LIGHT";

        System.out.println("✓ Formatted loading with transformation passed");
    }

    /**
     * Test 3: Nested object auto-copy.
     * OrbitData and AttackConfigData should be auto-copied from raw to formatted classes.
     */
    private static void testNestedObjectAutoCopy() {
        System.out.println("\n--- Test 3: Nested Object Auto-Copy ---");

        WeaponFormattedData formattedWeapon = YAMLLoader.load(
                "combat/testSword.yaml",
                "combat/weapon.yaml",
                WeaponRawData.class,
                WeaponFormattedData.class
        );

        // Verify ORBIT was auto-copied
        assert formattedWeapon.ORBIT != null : "ORBIT should be auto-copied";
        assert formattedWeapon.ORBIT.IMAGE_X == 0 : "ORBIT.IMAGE_X should match";
        assert formattedWeapon.ORBIT.ORBIT_DISTANCE == 15f : "ORBIT.ORBIT_DISTANCE should match";
        assert formattedWeapon.ORBIT.ORBIT_TYPE.equals("SWAP") : "ORBIT.ORBIT_TYPE should match";

        // Verify CONFIG was auto-copied
        assert formattedWeapon.CONFIG != null : "CONFIG should be auto-copied";
        assert formattedWeapon.CONFIG.containsKey("swingRegular") : "CONFIG should have swingRegular";

        WeaponFormattedData.AttackConfigData configData = formattedWeapon.CONFIG.get("swingRegular");
        assert configData != null : "swingRegular config should not be null";
        assert configData.PROJECTILE_DAMAGE != null && configData.PROJECTILE_DAMAGE.get(0) == 10 : "swingRegular PROJECTILE_DAMAGE should be auto-copied correctly";

        System.out.println("✓ Nested object auto-copy passed");
    }

    /**
     * Test 4: Combo tree transformation detail.
     * Verify the structure of transformed combo tree matches expected hierarchy.
     */
    private static void testComboTreeTransformation() {
        System.out.println("\n--- Test 4: Combo Tree Transformation Detail ---");

        WeaponFormattedData formattedWeapon = YAMLLoader.load(
                "combat/testSword.yaml",
                "combat/weapon.yaml",
                WeaponRawData.class,
                WeaponFormattedData.class
        );

        // First level: LIGHT with attack swingRegular
        List<AttackNode> comboTree = formattedWeapon.COMBO_TREE;
        assert comboTree.size() >= 1 : "Should have at least 1 root attack";

        AttackNode root = findAttackByName(comboTree, "swingRegular");
        assert root != null : "Should find swingRegular root attack";
        assert root.duration == AttackDuration.LIGHT : "swingRegular should be LIGHT duration";
        assert root.condition == null : "Root should have no condition (leaf/unconditional)";
        assert !root.isLeaf() : "swingRegular should have children in thenNext";

        // Second level: LIGHT -> swingRegular via thenNext
        AttackNode secondLevel = root.thenNext.get(AttackDuration.LIGHT);
        assert secondLevel != null : "Should find second swingRegular in combo";
        assert secondLevel.duration == AttackDuration.LIGHT : "Second level should be LIGHT";

        System.out.println("✓ Combo tree transformation detail passed");
    }

    /**
     * Test 5: Caching behavior.
     * Same file loaded twice should return cached instance.
     */
    private static void testCaching() {
        System.out.println("\n--- Test 5: Caching ---");

        // Clear cache to start fresh
        YAMLLoader.clearCache();

        // First load
        WeaponFormattedData weapon1 = YAMLLoader.load(
                "combat/testSword.yaml",
                "combat/weapon.yaml",
                WeaponRawData.class,
                WeaponFormattedData.class
        );

        // Second load - should return cached instance
        WeaponFormattedData weapon2 = YAMLLoader.load(
                "combat/testSword.yaml",
                "combat/weapon.yaml",
                WeaponRawData.class,
                WeaponFormattedData.class
        );

        // Verify they're the same object (cached)
        assert weapon1 == weapon2 : "Second load should return cached instance";

        // Raw loading should also cache separately
        WeaponRawData rawWeapon1 = YAMLLoader.load(
                "combat/testSword.yaml",
                "combat/weapon.yaml",
                WeaponRawData.class
        );

        WeaponRawData rawWeapon2 = YAMLLoader.load(
                "combat/testSword.yaml",
                "combat/weapon.yaml",
                WeaponRawData.class
        );

        assert rawWeapon1 == rawWeapon2 : "Raw loads should also use cache";

        System.out.println("✓ Caching passed");
    }

    // ===== Test YAML Files =====

    private static void writeSchemaYAML() throws IOException {
        writeFile("yaml/combat/weapon.yaml",
                "ORBIT:\n" +
                        "  IMAGE_X: 0\n" +
                        "  IMAGE_Y: 0\n" +
                        "  ORBIT_TYPE: SWAP\n" +
                        "  ORBIT_DISTANCE: 15\n" +
                        "  ORBIT_ITEM_ANIMATION_SPEED: 0.5\n" +
                        "  ORBIT_ITEM_ANIMATION_TYPE: SWING\n" +
                        "\n" +
                        "COMBO_TREE:\n" +
                        "  LIGHT:\n" +
                        "    ATTACK: swingRegular\n" +
                        "    IF: ANY\n" +
                        "    THEN_NEXT:\n" +
                        "      LIGHT:\n" +
                        "        ATTACK: swingRegular\n" +
                        "        IF: ANY\n" +
                        "\n" +
                        "AERIAL_COMBO_TREE:\n" +
                        "  LIGHT:\n" +
                        "    ATTACK: swingRegular\n" +
                        "    IF: ANY\n" +
                        "\n" +
                        "CONFIG:\n" +
                        "  swingRegular:\n" +
                        "    DAMAGE: 10\n"
        );
    }

    private static void writeInstanceYAML() throws IOException {
        writeFile("yaml/combat/testSword.yaml",
                "ORBIT:\n" +
                        "  IMAGE_X: 0\n" +
                        "\n" +
                        "COMBO_TREE:\n" +
                        "  LIGHT:\n" +
                        "    ATTACK: swingRegular\n" +
                        "    IF: ANY\n" +
                        "    THEN_NEXT:\n" +
                        "      LIGHT:\n" +
                        "        ATTACK: swingRegular\n" +
                        "        IF: ANY\n" +
                        "\n" +
                        "CONFIG:\n" +
                        "  swingRegular:\n" +
                        "    DAMAGE: 10\n"
        );
    }

    private static void writeSimpleTestYAML() throws IOException {
        // This is for potential future tests
    }

    private static void writeFile(String path, String content) throws IOException {
        FileHandle file = Gdx.files.local(path);
        file.parent().mkdirs();
        try (FileWriter writer = new FileWriter(file.file())) {
            writer.write(content);
        }
    }

    // ===== Helper Methods =====

    /**
     * Finds an attack node in a list by attack name.
     */
    private static AttackNode findAttackByName(List<AttackNode> nodes, String attackName) {
        for (AttackNode node : nodes) {
            if (node.attackName.equals(attackName)) {
                return node;
            }
        }
        return null;
    }
}
