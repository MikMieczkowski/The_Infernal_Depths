package com.mikm.rendering.cave;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessFiles;
import com.badlogic.gdx.backends.headless.HeadlessNativesLoader;
import com.badlogic.gdx.files.FileHandle;
import com.mikm.YamlCopyResolver;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class YamlCopyResolverTest {

    public static class WeaponConfig {
        public float IMAGE_X;
        public float IMAGE_Y;
        public float USAGE_TIME;
        public float COMBO_TIME;
        public int DAMAGE;
        public boolean CAN_BREAK_ROCKS;
        public List<Projectile> PROJECTILES;

        public static class Projectile {
            public String CREATE_ON;
            public String ANIMATION_NAME;
            public float SPEED;
            public Float FPS;
        }
    }

    public static void main(String[] args) throws IOException {
        HeadlessNativesLoader.load();
        Gdx.files = new HeadlessFiles();

        // write temporary YAML test files
        writeFile("copperSword1.yaml",
                "IMAGE_X: 0\n" +
                        "IMAGE_Y: 0\n" +
                        "USAGE_TIME: 0.4\n" +
                        "COMBO_TIME: 0\n" +
                        "DAMAGE: 1\n" +
                        "CAN_BREAK_ROCKS: false\n" +
                        "PROJECTILES:\n" +
                        "  - CREATE_ON: PRESS\n" +
                        "    ANIMATION_NAME: \"swordSlice\"\n" +
                        "    SPEED: 0\n");

        writeFile("copperSword2.yaml",
                "COPY: copperSword1.yaml\n" +
                        "COMBO_TIME: 1\n");

        writeFile("weaponList.yaml",
                "IMAGE_X: 0\n" +
                        "PROJECTILES:\n" +
                        "  - CREATE_ON: PRESS\n" +
                        "    ANIMATION_NAME: \"slice\"\n" +
                        "    SPEED: 0\n" +
                        "  - COPY: 0\n" +
                        "    SPEED: 1\n");

        writeFile("weaponMap.yaml",
                "map:\n" +
                        "  elem1:\n" +
                        "    data1: 1\n" +
                        "    data2: 2\n" +
                        "    data3:\n" +
                        "      data4: 4\n" +
                        "    CONFIG:\n" +
                        "      data1: 1\n" +
                        "      data2: 2\n" +
                        "      data3: 3\n" +
                        "  elem2:\n" +
                        "    data1: 1\n" +
                        "    data3:\n" +
                        "      data4: 4\n" +
                        "    COPY_CONFIG: elem1\n" +
                        "    CONFIG:\n" +
                        "      data2: 7\n");

        testSimpleCopy();
        testOverride();
        testListCopy();
        testMapSubfieldCopy();
        testDefinePreprocessor();

        System.out.println("All tests passed.");
    }

    private static void writeFile(String name, String content) throws IOException {
        FileHandle file = Gdx.files.local(name);
        try (FileWriter writer = new FileWriter(file.file())) {
            writer.write(content);
        }
    }

    private static void testSimpleCopy() {
        WeaponConfig config = YamlCopyResolver.loadAndResolve("copperSword2.yaml", WeaponConfig.class);
        assert config.DAMAGE == 1;
        assert config.COMBO_TIME == 1.0f;
        assert config.PROJECTILES.size() == 1;
        System.out.println("✓ testSimpleCopy passed");
    }

    private static void testOverride() {
        WeaponConfig config = YamlCopyResolver.loadAndResolve("copperSword2.yaml", WeaponConfig.class);
        assert config.COMBO_TIME == 1.0f;
        System.out.println("✓ testOverride passed");
    }

    private static void testListCopy() {
        WeaponConfig config = YamlCopyResolver.loadAndResolve("weaponList.yaml", WeaponConfig.class);
        assert config.PROJECTILES.size() == 2;
        assert config.PROJECTILES.get(1).SPEED == 1;
        System.out.println("✓ testListCopy passed");
    }

    private static void testMapSubfieldCopy() {
        Map resolved = YamlCopyResolver.loadAndResolve("weaponMap.yaml", Map.class);
        Map map = (Map) resolved.get("map");
        Map elem2 = (Map) map.get("elem2");
        Map config = (Map) elem2.get("CONFIG");
        assert config.get("data2").equals(7);
        assert config.get("data3").equals(3);
        System.out.println("testMapSubfieldCopy passed");
    }

    private static void testDefinePreprocessor() throws IOException {
        // Test basic #DEFINE substitution
        writeFile("defineTest.yaml",
                "#DEFINE SPEED_VAL 5.5\n" +
                        "#DEFINE DMG 10\n" +
                        "IMAGE_X: 0\n" +
                        "IMAGE_Y: 0\n" +
                        "USAGE_TIME: SPEED_VAL\n" +
                        "COMBO_TIME: SPEED_VAL\n" +
                        "DAMAGE: DMG\n" +
                        "CAN_BREAK_ROCKS: false\n");

        WeaponConfig config = YamlCopyResolver.loadAndResolve("defineTest.yaml", WeaponConfig.class);
        assert config.USAGE_TIME == 5.5f : "Expected USAGE_TIME=5.5, got " + config.USAGE_TIME;
        assert config.COMBO_TIME == 5.5f : "Expected COMBO_TIME=5.5, got " + config.COMBO_TIME;
        assert config.DAMAGE == 10 : "Expected DAMAGE=10, got " + config.DAMAGE;

        // Test that partial word matches are NOT replaced
        writeFile("definePartialTest.yaml",
                "#DEFINE X 99\n" +
                        "IMAGE_X: X\n" +
                        "IMAGE_Y: 0\n" +
                        "USAGE_TIME: 0.4\n" +
                        "COMBO_TIME: 0\n" +
                        "DAMAGE: 1\n" +
                        "CAN_BREAK_ROCKS: false\n");

        WeaponConfig config2 = YamlCopyResolver.loadAndResolve("definePartialTest.yaml", WeaponConfig.class);
        // IMAGE_X should NOT become IMAGE_99 because X is part of IMAGE_X
        assert config2.IMAGE_X == 99 : "Expected IMAGE_X=99 (X replaced in value), got " + config2.IMAGE_X;

        // Test preprocess directly for word boundary behavior
        String input = "#DEFINE VAR 123\nVAR VARIABLE VARX XVAR VAR_THING";
        String result = YamlCopyResolver.preprocess(input);
        // VAR should be replaced, but VARIABLE, VARX, XVAR, VAR_THING should NOT (underscore is part of word)
        assert result.equals("123 VARIABLE VARX XVAR VAR_THING") : "Unexpected result: " + result;

        System.out.println("testDefinePreprocessor passed");
    }
}
