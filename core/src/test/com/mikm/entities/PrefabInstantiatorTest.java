package com.mikm.entities;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.mikm.Vector2Int;
import com.mikm.YamlCopyResolver;
import com.mikm._components.CopyReference;
import com.mikm._components.Copyable;
import com.mikm._components.Transform;
import com.mikm._components.WorldColliderComponent;
import com.mikm._components.CombatComponent;
import com.mikm._components.SpriteComponent;
import com.mikm._components.ShadowComponent;
import com.mikm._components.routine.Routine;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm._components.routine.CycleStep;
import com.mikm._components.routine.BehaviourCycleStep;
import com.mikm._components.routine.Transition;
import com.mikm._components.routine.ConditionTransition;
import com.mikm.entities.actions.AcceleratedMoveAction;
import com.mikm.entities.actions.Action;
import com.mikm.entities.prefabLoader.EntityYAMLData;
import com.mikm.entities.prefabLoader.EntityYAMLReader;
import com.mikm.entities.prefabLoader.PrefabInstantiator;
import com.mikm.serialization.Serializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PrefabInstantiatorTest {
    @Test
    void shouldThrowError() {
        assertThrows(Exception.class, () -> {
            //Serializer.getInstance().copy(new RoutineListComponent());
        });
    }

    @Test
    void shouldCopyAccAction() {

        Serializer.getInstance(true);

        //TODO make this work with the tests
        Map<String, PrefabInstantiator.EntityInitFunction> entityNameToActionsInit = new HashMap<>();
        // Get original prefab components
        if (!PrefabInstantiator.getInstance().prefabExists("player")) {
            String fileName = "yaml/player.yaml";
            EntityYAMLData data = YamlCopyResolver.loadAndResolve(fileName, EntityYAMLData.class);
            PrefabInstantiator.getInstance().addPrefab("player", EntityYAMLReader.loadComponents(data, fileName, entityNameToActionsInit));
        }

        Iterable<Component> originalComponents = PrefabInstantiator.getInstance().nameToPrefab.get("player").components;

        // Get original components map for modification
        Map<Class<? extends Component>, Component> originalComponentsMap = new HashMap<>();
        for (Component c : originalComponents) {
            originalComponentsMap.put(c.getClass(), c);
        }
        RoutineListComponent r = (RoutineListComponent) originalComponentsMap.get(RoutineListComponent.class);
        AcceleratedMoveAction a = (AcceleratedMoveAction) r.getRoutine("start").cycleSteps.get(0).getAction();
        System.out.println(a.name);

        AcceleratedMoveAction clone = PrefabInstantiator.getInstance().cloneRecursiveLog(a);

        System.out.println(clone.name);
    }

    @Test
    void shouldCopy() {
        Serializer.getInstance(true);
        Transform transform = new Transform(3,4);
        transform.FULL_BOUNDS_DIMENSIONS = new Vector2Int(1,2);
        transform.ORIGIN_X = 1;
        Transform copy;
        try {
            copy = PrefabInstantiator.getInstance().cloneRecursive(transform);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // shallow/simple fields copied correctly
        assert transform.ORIGIN_X == copy.ORIGIN_X;
        // deep object cloned separately but equal
        assert transform.FULL_BOUNDS_DIMENSIONS.x == copy.FULL_BOUNDS_DIMENSIONS.x
            && transform.FULL_BOUNDS_DIMENSIONS.y == copy.FULL_BOUNDS_DIMENSIONS.y;
        assert transform.FULL_BOUNDS_DIMENSIONS != copy.FULL_BOUNDS_DIMENSIONS;

        // untouched defaults preserved
        assert transform.x != copy.x;
        assert copy.x == 0;
        assert transform != copy;


        CopyTest orig = new CopyTest();
        orig.x = 5;
        orig.c1.get(0).get(0).x = 5;
        CopyTest copyTest;
        try {
            copyTest = PrefabInstantiator.getInstance().cloneRecursive(orig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // deep copy: different object but equal data
        assert copyTest != orig;
        assert copyTest.c1 != orig.c1;
        assert copyTest.c1.size() == orig.c1.size();
        assert copyTest.c1.get(0) != orig.c1.get(0);
        assert copyTest.c1.get(0).get(0).l != orig.c1.get(0).get(0).l; // deep copy of nested list
        assert copyTest.c1.get(0).get(0).l.equals(orig.c1.get(0).get(0).l);

        // shallow copy: same reference
        assert copyTest.l2 == orig.l2;
        assert copyTest.c1.get(0).get(0).l2 == orig.c1.get(0).get(0).l2;

        // deep copy: separate reference but same contents
        assert copyTest.l != orig.l;
        assert copyTest.l.equals(orig.l);

        assert copyTest.x == 0;
        assert copyTest.c1.get(0).get(0).x == 0;
        assert copyTest != orig;
        assert copyTest.c1.get(0).get(0) != orig.c1.get(0).get(0);
        assert copyTest.c1.get(0).get(0).l != orig.c1.get(0).get(0).l;
        assert copyTest.c1.get(0).get(0).l2 == orig.c1.get(0).get(0).l2;



    }


    public static class CopyTest {
        @Copyable
        List<List<Integer>> l = new ArrayList<>(List.of(
                new ArrayList<>(List.of(1, 2)),
                new ArrayList<>(List.of(3, 4)),
                new ArrayList<>(List.of(5, 6))
        ));

        @CopyReference
        List<List<Integer>> l2 = new ArrayList<>(List.of(
                new ArrayList<>(List.of(1, 2)),
                new ArrayList<>(List.of(3, 4)),
                new ArrayList<>(List.of(5, 6))
        ));

        int x = 0;

        @Copyable
        List<List<CopyTestInner>> c1 = new ArrayList<>(List.of(
                new ArrayList<>(List.of(new CopyTestInner(), new CopyTestInner())),
                new ArrayList<>(List.of(new CopyTestInner(), new CopyTestInner())),
                new ArrayList<>(List.of(new CopyTestInner(), new CopyTestInner()))
        ));

        public static class CopyTestInner {
            public CopyTestInner() {}

            @Copyable
            List<List<Integer>> l = new ArrayList<>(List.of(
                    new ArrayList<>(List.of(1, 2)),
                    new ArrayList<>(List.of(3, 4)),
                    new ArrayList<>(List.of(5, 6))
            ));

            @CopyReference
            List<List<Integer>> l2 = new ArrayList<>(List.of(
                    new ArrayList<>(List.of(1, 2)),
                    new ArrayList<>(List.of(3, 4)),
                    new ArrayList<>(List.of(5, 6))
            ));

            int x = 0;
        }
    }


    @BeforeAll
    static void setupGdx() {
        GdxTestBootstrap.init();
        Gdx.gl = mock(GL20.class);
        Gdx.gl20 = Gdx.gl;
        Gdx.files = new Files() {
            @Override
            public FileHandle getFileHandle(String path, FileType type) {
                return null;
            }

            @Override
            public FileHandle classpath(String path) {
                return null;
            }

            @Override
            public FileHandle internal(String path) {
                return new FileHandle("D:/IntelliJprojects\\The_Infernal_Depths-master(1)\\The_Infernal_Depths-master\\assets\\" + path);
            }

            @Override
            public FileHandle external(String path) {
                return null;
            }

            @Override
            public FileHandle absolute(String path) {
                return null;
            }

            @Override
            public FileHandle local(String path) {
                return new FileHandle("D:/IntelliJprojects\\The_Infernal_Depths-master(1)\\The_Infernal_Depths-master\\assets\\" + path);
            }

            @Override
            public String getExternalStoragePath() {
                return null;
            }

            @Override
            public boolean isExternalStorageAvailable() {
                return false;
            }

            @Override
            public String getLocalStoragePath() {
                return null;
            }

            @Override
            public boolean isLocalStorageAvailable() {
                return false;
            }
        };
    }

    @Test
    void shouldClonePlayerCorrectly() {
        Serializer.getInstance(true);

        //TODO make this work with the tests
        Map<String, PrefabInstantiator.EntityInitFunction> entityNameToActionsInit = new HashMap<>();
        // Get original prefab components
        if (!PrefabInstantiator.getInstance().prefabExists("player")) {
            String fileName = "yaml/player.yaml";
            EntityYAMLData data = YamlCopyResolver.loadAndResolve(fileName, EntityYAMLData.class);
            PrefabInstantiator.getInstance().addPrefab("player", EntityYAMLReader.loadComponents(data, fileName, entityNameToActionsInit));
        }
        
        Set<Component> originalComponents = new HashSet<>((Collection) PrefabInstantiator.getInstance().nameToPrefab.get("player").components);
        assertNotNull(originalComponents, "Original components should not be null");
        
        // Get original components map for modification
        Map<Class<? extends Component>, Component> originalComponentsMap = new HashMap<>();
        for (Component c : originalComponents) {
            originalComponentsMap.put(c.getClass(), c);
        }
        
        // Modify non-annotated fields before cloning to verify they are NOT copied
        // (When a class has @Copyable/@CopyReference fields, only those are copied)
        Transform originalTransform = (Transform) originalComponentsMap.get(Transform.class);
        if (originalTransform != null) {
            originalTransform.x = 100f;
            originalTransform.y = 200f;
            originalTransform.xVel = 5f;
            originalTransform.yVel = 10f;
            originalTransform.rotation = 45f;
            originalTransform.height = 20f;
        }
        
        WorldColliderComponent originalCollider = (WorldColliderComponent) originalComponentsMap.get(WorldColliderComponent.class);
        if (originalCollider != null) {
            originalCollider.tilePosition = new Vector2Int(50, 60);
            originalCollider.nextPosition = new com.badlogic.gdx.math.Vector2(100f, 200f);
        }
        
        CombatComponent originalCombat = (CombatComponent) originalComponentsMap.get(CombatComponent.class);
        if (originalCombat != null) {
            originalCombat.hp = 999; // This should not be copied
            originalCombat.isAttackable = false; // This should not be copied
        }
        
        RoutineListComponent originalRoutineList = (RoutineListComponent) originalComponentsMap.get(RoutineListComponent.class);
        if (originalRoutineList != null && !originalRoutineList.routines.isEmpty()) {
            // currentRoutine is not annotated, so it should not be copied
            // We can't easily test this without setting it, but we can verify it's null/default in clone
        }
        
        // Clone all components
        Map<Class<? extends Component>, Component> clonedComponents = createComp("player");
        
        // Verify all components were cloned
        assertEquals(originalComponents.size(), clonedComponents.size(), 
            "All components should be cloned");
        
        // Test Transform component
        Transform clonedTransform = (Transform) clonedComponents.get(Transform.class);
        assertNotNull(originalTransform, "Original Transform should exist");
        assertNotNull(clonedTransform, "Cloned Transform should exist");
        assertNotSame(originalTransform, clonedTransform, "Transform should be a different object");
        
        // Test @Copyable fields in Transform - should be recursively cloned
        assertNotNull(clonedTransform.FULL_BOUNDS_DIMENSIONS, "FULL_BOUNDS_DIMENSIONS should be cloned");
        assertNotSame(originalTransform.FULL_BOUNDS_DIMENSIONS, clonedTransform.FULL_BOUNDS_DIMENSIONS,
            "FULL_BOUNDS_DIMENSIONS should be a different object (recursively cloned)");
        assertEquals(originalTransform.FULL_BOUNDS_DIMENSIONS.x, clonedTransform.FULL_BOUNDS_DIMENSIONS.x,
            "FULL_BOUNDS_DIMENSIONS.x should match");
        assertEquals(originalTransform.FULL_BOUNDS_DIMENSIONS.y, clonedTransform.FULL_BOUNDS_DIMENSIONS.y,
            "FULL_BOUNDS_DIMENSIONS.y should match");
        assertEquals(originalTransform.ORIGIN_X, clonedTransform.ORIGIN_X, "ORIGIN_X should match");
        assertEquals(originalTransform.ORIGIN_Y, clonedTransform.ORIGIN_Y, "ORIGIN_Y should match");
        assertEquals(originalTransform.ENTITY_NAME, clonedTransform.ENTITY_NAME, "ENTITY_NAME should match");
        
        // Assert values from player.yaml
        assertEquals("player", clonedTransform.ENTITY_NAME, "ENTITY_NAME should be 'player' from yaml");
        assertEquals(32, clonedTransform.FULL_BOUNDS_DIMENSIONS.x, "FULL_BOUNDS_DIMENSIONS.x should be 32 from yaml");
        assertEquals(32, clonedTransform.FULL_BOUNDS_DIMENSIONS.y, "FULL_BOUNDS_DIMENSIONS.y should be 32 from yaml");
        
        // Test non-annotated fields in Transform - should NOT be copied when class has @Copyable fields
        // Even though originalTransform.x = 100, y = 200, these should be default (0) in clone
        assertEquals(0f, clonedTransform.x, "x should be default value (0), not copied from original (100)");
        assertEquals(0f, clonedTransform.y, "y should be default value (0), not copied from original (200)");
        assertEquals(0f, clonedTransform.xVel, "xVel should be default value (0), not copied from original (5)");
        assertEquals(0f, clonedTransform.yVel, "yVel should be default value (0), not copied from original (10)");
        assertEquals(0f, clonedTransform.rotation, "rotation should be default value (0), not copied from original (45)");
        assertEquals(0f, clonedTransform.height, "height should be default value (0), not copied from original (20)");
        
        // Test ColliderComponent
        WorldColliderComponent clonedCollider = (WorldColliderComponent) clonedComponents.get(WorldColliderComponent.class);
        assertNotNull(originalCollider, "Original ColliderComponent should exist");
        assertNotNull(clonedCollider, "Cloned ColliderComponent should exist");
        assertNotSame(originalCollider, clonedCollider, "ColliderComponent should be a different object");
        
        // Test @Copyable fields in ColliderComponent
        assertNotNull(clonedCollider.HITBOX_OFFSETS, "HITBOX_OFFSETS should be cloned");
        assertNotSame(originalCollider.HITBOX_OFFSETS, clonedCollider.HITBOX_OFFSETS,
            "HITBOX_OFFSETS should be a different object (recursively cloned)");
        assertEquals(originalCollider.HITBOX_OFFSETS.x, clonedCollider.HITBOX_OFFSETS.x,
            "HITBOX_OFFSETS.x should match");
        assertEquals(originalCollider.HITBOX_OFFSETS.y, clonedCollider.HITBOX_OFFSETS.y,
            "HITBOX_OFFSETS.y should match");
        assertEquals(originalCollider.RADIUS, clonedCollider.RADIUS, "RADIUS should match");
        assertEquals(originalCollider.IS_BAT, clonedCollider.IS_BAT, "IS_BAT should match");
        
        // Assert values from player.yaml
        assertEquals(7f, clonedCollider.RADIUS, "RADIUS should be 7 from yaml (HITBOX_RADIUS)");
        assertFalse(clonedCollider.IS_BAT, "IS_BAT should be false for player");
        
        // Test non-annotated fields in ColliderComponent - should NOT be copied
        // tilePosition and nextPosition should be default/new instances, not the modified values
        assertNotNull(clonedCollider.tilePosition, "tilePosition should exist but be a new instance");
        if (originalCollider != null && originalCollider.tilePosition != null) {
            // The cloned tilePosition should be a different object (or default values)
            // Since it's not annotated, it should be a new instance with default values
            assertNotSame(originalCollider.tilePosition, clonedCollider.tilePosition,
                "tilePosition should be a different instance (not copied by reference)");
        }
        
        // Test CombatComponent
        CombatComponent clonedCombat = (CombatComponent) clonedComponents.get(CombatComponent.class);
        assertNotNull(originalCombat, "Original CombatComponent should exist");
        assertNotNull(clonedCombat, "Cloned CombatComponent should exist");
        assertNotSame(originalCombat, clonedCombat, "CombatComponent should be a different object");
        
        // Test @Copyable fields in CombatComponent
        assertEquals(originalCombat.MAX_HP, clonedCombat.MAX_HP, "MAX_HP should match");
        assertEquals(originalCombat.DAMAGE, clonedCombat.DAMAGE, "DAMAGE should match");
        assertEquals(originalCombat.KNOCKBACK, clonedCombat.KNOCKBACK, "KNOCKBACK should match");
        assertEquals(originalCombat.HURT_SOUND_EFFECT, clonedCombat.HURT_SOUND_EFFECT, "HURT_SOUND_EFFECT should match");
        
        // Assert values from player.yaml
        assertEquals(50, clonedCombat.MAX_HP, "MAX_HP should be 50 from yaml");
        assertEquals(10, clonedCombat.DAMAGE, "DAMAGE should be 10 from yaml");
        assertEquals(2, clonedCombat.KNOCKBACK, "KNOCKBACK should be 2 from yaml");
        assertEquals("playerHit", clonedCombat.HURT_SOUND_EFFECT, "HURT_SOUND_EFFECT should be 'playerHit' from yaml");
        
        // Test @CopyReference field in CombatComponent - should be same reference
        assertSame(originalCombat.HURT_ANIMATION, clonedCombat.HURT_ANIMATION,
            "HURT_ANIMATION should be the same reference (CopyReference)");
        
        // Test non-annotated fields in CombatComponent - should NOT be copied
        // hp and isAttackable should have default values, not the modified values
        if (originalCombat != null) {
            // hp should be set to MAX_HP by EntityLoader, not the modified 999
            //assertEquals(clonedCombat.MAX_HP, clonedCombat.hp,
           //     "hp should be set to MAX_HP (50), not copied from modified original (999)");
            // isAttackable should be true (from yaml/EntityLoader logic), not the modified false
            //TODO
            //assertTrue(clonedCombat.isAttackable,
            //    "isAttackable should be true (from EntityLoader), not copied from modified original (false)");
        }
        
        // Test RoutineListComponent
        RoutineListComponent clonedRoutineList = (RoutineListComponent) clonedComponents.get(RoutineListComponent.class);
        assertNotNull(originalRoutineList, "Original RoutineListComponent should exist");
        assertNotNull(clonedRoutineList, "Cloned RoutineListComponent should exist");
        assertNotSame(originalRoutineList, clonedRoutineList, "RoutineListComponent should be a different object");
        
        // Test @Copyable fields in RoutineListComponent
        assertEquals(originalTransform.SPEED, clonedTransform.SPEED, "SPEED should match");
        assertEquals(originalRoutineList.CHECK_TRANSITIONS_EVERY_FRAME, clonedRoutineList.CHECK_TRANSITIONS_EVERY_FRAME,
            "CHECK_TRANSITIONS_EVERY_FRAME should match");
        
        // Assert values from player.yaml
        assertEquals(2f, clonedTransform.SPEED, "SPEED should be 2 from yaml");
        assertTrue(clonedRoutineList.CHECK_TRANSITIONS_EVERY_FRAME, "CHECK_TRANSITIONS_EVERY_FRAME should be true from yaml");
        
        // Test routines list - should be recursively cloned
        assertNotNull(clonedRoutineList.routines, "routines should be cloned");
        assertNotSame(originalRoutineList.routines, clonedRoutineList.routines,
            "routines list should be a different object");
        assertEquals(originalRoutineList.routines.size(), clonedRoutineList.routines.size(),
            "routines list size should match");
        
        // Test that routines themselves are cloned recursively
        if (!originalRoutineList.routines.isEmpty()) {
            Routine originalRoutine = originalRoutineList.routines.get(0);
            Routine clonedRoutine = clonedRoutineList.routines.get(0);
            assertNotSame(originalRoutine, clonedRoutine, "Routine should be a different object");
            
            // Test @Copyable fields in Routine
            assertEquals(originalRoutine.name, clonedRoutine.name, "Routine name should match");
            assertNotSame(originalRoutine.cycleSteps, clonedRoutine.cycleSteps,
                "cycleSteps should be a different object");
            assertEquals(originalRoutine.cycleSteps.size(), clonedRoutine.cycleSteps.size(),
                "cycleSteps size should match");
            
            // Test cycleSteps - should be recursively cloned
            if (!originalRoutine.cycleSteps.isEmpty()) {
                CycleStep originalCycleStep = originalRoutine.cycleSteps.get(0);
                CycleStep clonedCycleStep = clonedRoutine.cycleSteps.get(0);
                assertNotSame(originalCycleStep, clonedCycleStep, "CycleStep should be a different object");
                
                // Test BehaviourCycleStep with @Copyable Action
                if (originalCycleStep instanceof BehaviourCycleStep) {
                    BehaviourCycleStep originalBehaviourStep = (BehaviourCycleStep) originalCycleStep;
                    BehaviourCycleStep clonedBehaviourStep = (BehaviourCycleStep) clonedCycleStep;
                    
                    // Action should be recursively cloned
                    assertNotSame(originalBehaviourStep.getAction(), clonedBehaviourStep.getAction(),
                        "Action should be a different object (recursively cloned)");
                    
                    // Test @Copyable fields in Action
                    // NOTE: This test may fail because getDeclaredFields() only returns fields declared in the class itself,
                    // not inherited fields. Since 'name' is declared in Action (parent class) and we're cloning a subclass
                    // like AcceleratedMoveAction, the name field won't be found and won't be copied.
                    // This is a known bug in PrefabLoader.cloneRecursive() - it should iterate through the class hierarchy.
                        assertEquals(originalBehaviourStep.getAction().name, clonedBehaviourStep.getAction().name,
                            "Action name should match (this may fail due to getDeclaredFields() not including inherited fields)");
                    
                    // Test @CopyReference field in Action - animation should be same reference
                    assertSame(originalBehaviourStep.getAction().animation, clonedBehaviourStep.getAction().animation,
                        "Action animation should be the same reference (CopyReference)");
                }
            }
            
            // Test transitions
            assertNotSame(originalRoutine.transitions, clonedRoutine.transitions,
                "transitions should be a different object");
            assertEquals(originalRoutine.transitions.size(), clonedRoutine.transitions.size(),
                "transitions size should match");
        }
        
        // Test DamagedAction - should be recursively cloned
        assertNotNull(clonedRoutineList.damagedAction, "damagedAction should be cloned");
        assertNotSame(originalRoutineList.damagedAction, clonedRoutineList.damagedAction,
            "damagedAction should be a different object");
        
        // Test usedActionClasses Set - should be recursively cloned
        assertNotNull(clonedRoutineList.usedActionClasses, "usedActionClasses should be cloned");
        assertNotSame(originalRoutineList.usedActionClasses, clonedRoutineList.usedActionClasses,
            "usedActionClasses should be a different object");
        assertEquals(originalRoutineList.usedActionClasses.size(), clonedRoutineList.usedActionClasses.size(),
            "usedActionClasses size should match");
        
        // Test SpriteComponent - has no annotations, so all fields should be deep copied
        SpriteComponent originalSprite = (SpriteComponent) originalComponentsMap.get(SpriteComponent.class);
        SpriteComponent clonedSprite = (SpriteComponent) clonedComponents.get(SpriteComponent.class);
        if (originalSprite != null && clonedSprite != null) {
            assertNotSame(originalSprite, clonedSprite, "SpriteComponent should be a different object");
            // Non-annotated fields should be deep copied (or copied by Kryo if that's implemented)
            // Since SpriteComponent has no annotations, all fields should be copied
        }
        
        // Test ShadowComponent if it exists
        ShadowComponent originalShadow = (ShadowComponent) originalComponentsMap.get(ShadowComponent.class);
        ShadowComponent clonedShadow = (ShadowComponent) clonedComponents.get(ShadowComponent.class);
        if (originalShadow != null && clonedShadow != null) {
            assertNotSame(originalShadow, clonedShadow, "ShadowComponent should be a different object");
            // Test @Copyable BOUNDS_OFFSETS
            if (originalShadow.BOUNDS_OFFSETS != null) {
                assertNotNull(clonedShadow.BOUNDS_OFFSETS, "BOUNDS_OFFSETS should be cloned");
                assertNotSame(originalShadow.BOUNDS_OFFSETS, clonedShadow.BOUNDS_OFFSETS,
                    "BOUNDS_OFFSETS should be a different object (recursively cloned)");
                
                // Assert values from player.yaml
                assertEquals(8f, clonedShadow.BOUNDS_OFFSETS.x, "BOUNDS_OFFSETS.x should be 8 from yaml (SHADOW_OFFSET_X)");
                assertEquals(6f, clonedShadow.BOUNDS_OFFSETS.y, "BOUNDS_OFFSETS.y should be 6 from yaml (SHADOW_OFFSET_Y)");
            }
        }
    }

    @Test
    void shouldMaintainSharedReferencesInClonedComponents() {
        Serializer.getInstance(true);

        //TODO make this work with the tests
        Map<String, PrefabInstantiator.EntityInitFunction> entityNameToActionsInit = new HashMap<>();
        // Get original prefab components
        if (!PrefabInstantiator.getInstance().prefabExists("player")) {
            String fileName = "yaml/player.yaml";
            EntityYAMLData data = YamlCopyResolver.loadAndResolve(fileName, EntityYAMLData.class);
            PrefabInstantiator.getInstance().addPrefab("player", EntityYAMLReader.loadComponents(data, fileName, entityNameToActionsInit));
        }
        
        // Clone all components
        Map<Class<? extends Component>, Component> clonedComponents = createComp("player");
        RoutineListComponent clonedRoutineList = (RoutineListComponent) clonedComponents.get(RoutineListComponent.class);
        assertNotNull(clonedRoutineList, "RoutineListComponent should exist");
        assertNotNull(clonedRoutineList.routines, "routines should exist");
        assertFalse(clonedRoutineList.routines.isEmpty(), "routines should not be empty");
        
        // Create a map of routine names to Routine instances for easy lookup
        Map<String, Routine> nameToRoutine = new HashMap<>();
        for (Routine routine : clonedRoutineList.routines) {
            nameToRoutine.put(routine.name, routine);
        }
        
        // Test 1: BehaviourCycleStep.action should reference the same Action instance
        // that is used in other places (if the same action is referenced multiple times)
        for (Routine routine : clonedRoutineList.routines) {
            if (routine.cycleSteps != null) {
                for (CycleStep cycleStep : routine.cycleSteps) {
                    if (cycleStep instanceof BehaviourCycleStep) {
                        BehaviourCycleStep behaviourStep = (BehaviourCycleStep) cycleStep;
                        Action action = behaviourStep.getAction();
                        assertNotNull(action, "Action should not be null");
                        
                        // Check if this action is referenced in ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO
                        if (action.ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO != null) {
                            // The routine referenced should be the same instance from the routines list
                            Routine referencedRoutine = action.ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO;
                            Routine routineFromList = nameToRoutine.get(referencedRoutine.name);
                            assertNotNull(routineFromList, "Routine " + referencedRoutine.name + " should exist in routines list");
                            assertSame(routineFromList, referencedRoutine,
                                "ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO should reference the same Routine instance from routines list");
                        }
                    }
                }
            }
        }
        
        // Test 2: ConditionTransition.goTo should reference the same Routine instance from routines list
        for (Routine routine : clonedRoutineList.routines) {
            if (routine.transitions != null) {
                for (Transition transition : routine.transitions) {
                    if (transition instanceof ConditionTransition) {
                        ConditionTransition conditionTransition = (ConditionTransition) transition;
                        Routine goToRoutine = conditionTransition.getGoToRoutine();
                        if (goToRoutine != null) {
                            Routine routineFromList = nameToRoutine.get(goToRoutine.name);
                            assertNotNull(routineFromList, "Routine " + goToRoutine.name + " should exist in routines list");
                            assertSame(routineFromList, goToRoutine,
                                "ConditionTransition.goTo should reference the same Routine instance from routines list");
                        }
                        
                        // Test randomRoutines (using reflection to access private field)
                        try {
                            java.lang.reflect.Field randomRoutinesField = ConditionTransition.class.getDeclaredField("randomRoutines");
                            randomRoutinesField.setAccessible(true);
                            @SuppressWarnings("unchecked")
                            List<Routine> randomRoutines = (List<Routine>) randomRoutinesField.get(conditionTransition);
                            if (randomRoutines != null) {
                                for (Routine randomRoutine : randomRoutines) {
                                    Routine routineFromList = nameToRoutine.get(randomRoutine.name);
                                    assertNotNull(routineFromList, "Random Routine " + randomRoutine.name + " should exist in routines list");
                                    assertSame(routineFromList, randomRoutine,
                                        "ConditionTransition.randomRoutines should reference the same Routine instances from routines list");
                                }
                            }
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            // Field might not exist or be accessible, skip this test
                        }
                        
                        // Test weightedRandomRoutines (using reflection to access private field)
                        try {
                            java.lang.reflect.Field weightedRandomRoutinesField = ConditionTransition.class.getDeclaredField("weightedRandomRoutines");
                            weightedRandomRoutinesField.setAccessible(true);
                            @SuppressWarnings("unchecked")
                            LinkedHashMap<Routine, Float> weightedRandomRoutines = (LinkedHashMap<Routine, Float>) weightedRandomRoutinesField.get(conditionTransition);
                            if (weightedRandomRoutines != null) {
                                for (Routine weightedRoutine : weightedRandomRoutines.keySet()) {
                                    Routine routineFromList = nameToRoutine.get(weightedRoutine.name);
                                    assertNotNull(routineFromList, "Weighted Routine " + weightedRoutine.name + " should exist in routines list");
                                    assertSame(routineFromList, weightedRoutine,
                                        "ConditionTransition.weightedRandomRoutines should reference the same Routine instances from routines list");
                                }
                            }
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            // Field might not exist or be accessible, skip this test
                        }
                    }
                }
            }
        }
        
        // Test 3: NoRepeatTransition.routine should reference the same Routine instance from routines list
        for (Routine routine : clonedRoutineList.routines) {
            if (routine.transitions != null) {
                for (Transition transition : routine.transitions) {
                    if (transition instanceof com.mikm._components.routine.NoRepeatTransition) {
                        com.mikm._components.routine.NoRepeatTransition noRepeatTransition = 
                            (com.mikm._components.routine.NoRepeatTransition) transition;
                        Routine goToRoutine = noRepeatTransition.getGoToRoutine();
                        if (goToRoutine != null) {
                            Routine routineFromList = nameToRoutine.get(goToRoutine.name);
                            assertNotNull(routineFromList, "NoRepeat Routine " + goToRoutine.name + " should exist in routines list");
                            assertSame(routineFromList, goToRoutine,
                                "NoRepeatTransition.routine should reference the same Routine instance from routines list");
                        }
                    }
                }
            }
        }
        
        // Test 4: RandomCycleStep.actions should reference the same Action instances
        // that are used in BehaviourCycleStep within the same or other routines
        Map<String, Action> nameToAction = new HashMap<>();
        // First, collect all actions from BehaviourCycleStep
        for (Routine routine : clonedRoutineList.routines) {
            if (routine.cycleSteps != null) {
                for (CycleStep cycleStep : routine.cycleSteps) {
                    if (cycleStep instanceof BehaviourCycleStep) {
                        BehaviourCycleStep behaviourStep = (BehaviourCycleStep) cycleStep;
                        Action action = behaviourStep.getAction();
                        if (action != null && action.name != null) {
                            nameToAction.put(action.name, action);
                        }
                    }
                }
            }
        }
        
        // Now check RandomCycleStep actions
        for (Routine routine : clonedRoutineList.routines) {
            if (routine.cycleSteps != null) {
                for (CycleStep cycleStep : routine.cycleSteps) {
                    if (cycleStep instanceof com.mikm._components.routine.RandomCycleStep) {
                        com.mikm._components.routine.RandomCycleStep randomStep = 
                            (com.mikm._components.routine.RandomCycleStep) cycleStep;
                        // Access actions field (package-private, accessible via reflection if needed)
                        try {
                            java.lang.reflect.Field actionsField = com.mikm._components.routine.RandomCycleStep.class.getDeclaredField("actions");
                            actionsField.setAccessible(true);
                            @SuppressWarnings("unchecked")
                            List<Action> actions = (List<Action>) actionsField.get(randomStep);
                            if (actions != null) {
                                for (Action action : actions) {
                                    if (action != null && action.name != null) {
                                        Action actionFromMap = nameToAction.get(action.name);
                                        if (actionFromMap != null) {
                                            assertSame(actionFromMap, action,
                                                "RandomCycleStep.actions should reference the same Action instances as BehaviourCycleStep");
                                        }
                                    }
                                }
                            }
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            // Field might not exist or be accessible, skip this test
                        }
                    }
                }
            }
        }
        
        // Test 5: WeightedRandomCycleStep.weightedRandom should reference the same Action instances
        for (Routine routine : clonedRoutineList.routines) {
            if (routine.cycleSteps != null) {
                for (CycleStep cycleStep : routine.cycleSteps) {
                    if (cycleStep instanceof com.mikm._components.routine.WeightedRandomCycleStep) {
                        com.mikm._components.routine.WeightedRandomCycleStep weightedStep = 
                            (com.mikm._components.routine.WeightedRandomCycleStep) cycleStep;
                        // Access weightedRandom field (package-private, accessible via reflection if needed)
                        try {
                            java.lang.reflect.Field weightedRandomField = com.mikm._components.routine.WeightedRandomCycleStep.class.getDeclaredField("weightedRandom");
                            weightedRandomField.setAccessible(true);
                            @SuppressWarnings("unchecked")
                            Map<Action, Float> weightedRandom = (Map<Action, Float>) weightedRandomField.get(weightedStep);
                            if (weightedRandom != null) {
                                for (Action action : weightedRandom.keySet()) {
                                    if (action != null && action.name != null) {
                                        Action actionFromMap = nameToAction.get(action.name);
                                        if (actionFromMap != null) {
                                            assertSame(actionFromMap, action,
                                                "WeightedRandomCycleStep.weightedRandom should reference the same Action instances as BehaviourCycleStep");
                                        }
                                    }
                                }
                            }
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            // Field might not exist or be accessible, skip this test
                        }
                    }
                }
            }
        }
        
        // Test 6: Routine.cycleSteps and transitions should contain Actions/Routines that are shared
        // This is more of a structural test - we've already tested the individual cases above
        
        // Test 7: RoutineListComponent.damagedAction should be properly cloned
        // (damagedAction is @Copyable, so it should be cloned, but any references within it should be shared)
        assertNotNull(clonedRoutineList.damagedAction, "damagedAction should be cloned");
        
        // Test 8: Verify that Actions in cycleSteps reference Routines from the routines list
        // (if they have ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO set)
        for (Routine routine : clonedRoutineList.routines) {
            if (routine.cycleSteps != null) {
                for (CycleStep cycleStep : routine.cycleSteps) {
                    Action action = cycleStep.getAction();
                    if (action != null && action.ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO != null) {
                        Routine referencedRoutine = action.ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO;
                        Routine routineFromList = nameToRoutine.get(referencedRoutine.name);
                        assertNotNull(routineFromList, "Referenced routine " + referencedRoutine.name + " should exist");
                        assertSame(routineFromList, referencedRoutine,
                            "Action.ON_HITTING_PLAYER_INTERRUPT_AND_GO_TO should reference the same Routine instance from routines list");
                    }
                }
            }
        }
        
        // Test 9: Verify that the same Action instance is used when referenced in multiple places
        // (e.g., if "Walk" action is used in both a BehaviourCycleStep and a RandomCycleStep,
        // they should reference the same Action instance)
        Map<String, Action> actionInstancesByName = new HashMap<>();
        for (Routine routine : clonedRoutineList.routines) {
            if (routine.cycleSteps != null) {
                for (CycleStep cycleStep : routine.cycleSteps) {
                    Action action = cycleStep.getAction();
                    if (action != null && action.name != null) {
                        Action existingAction = actionInstancesByName.get(action.name);
                        if (existingAction != null) {
                            // If we've seen this action name before, it should be the same instance
                            //assertSame(existingAction, action,
                            //    "Action '" + action.name + "' should be the same instance when used in multiple CycleSteps");
                        } else {
                            actionInstancesByName.put(action.name, action);
                        }
                    }
                }
            }
        }
        
        // Test 10: Verify that when the same Routine is referenced from multiple transitions,
        // they all point to the same instance
        Map<String, Routine> routineInstancesByName = new HashMap<>();
        for (Routine routine : clonedRoutineList.routines) {
            routineInstancesByName.put(routine.name, routine);
        }


        for (Routine routine : clonedRoutineList.routines) {
            if (routine.transitions != null) {
                for (Transition transition : routine.transitions) {
                    Routine goToRoutine = transition.getGoToRoutine();
                    if (goToRoutine != null) {
                        Routine expectedRoutine = routineInstancesByName.get(goToRoutine.name);
                        assertNotNull(expectedRoutine, "Routine " + goToRoutine.name + " should exist");
                        assertSame(expectedRoutine, goToRoutine,
                                "Transition.getGoToRoutine() should return the same Routine instance as in routines list");
                    }
                }
            }
        }
    }

    private Map<Class<? extends Component>, Component> addEntity(String entityName) {

        //TODO make this work with the tests
        Map<String, PrefabInstantiator.EntityInitFunction> entityNameToActionsInit = new HashMap<>();
        //Create prefab from yaml file if not already created, then create entity from prefab
        if (!PrefabInstantiator.getInstance().prefabExists(entityName)) {
            String fileName = "yaml/" + entityName + ".yaml";
            EntityYAMLData data = YamlCopyResolver.loadAndResolve(fileName, EntityYAMLData.class);
            PrefabInstantiator.getInstance().addPrefab(entityName, EntityYAMLReader.loadComponents(data, fileName, entityNameToActionsInit));
        }
        return createComp(entityName);
    }

    private Map<Class<? extends Component>, Component> createComp(String type) {
        Map<Class<? extends Component>, Component> components = new HashMap<>();
        for (Component c : PrefabInstantiator.getInstance().nameToPrefab.get(type).components) {
            Component cloned;
            try {
                cloned = PrefabInstantiator.getInstance().cloneRecursive(c);
            } catch (Exception ex) {

                ex.printStackTrace();
                throw new RuntimeException("Could not clone component " + c.getClass().getSimpleName() + " for entity " + type + ". " + ex);
            }
            components.put(cloned.getClass(), cloned);
            //cloneComp.runtimeInit()?
        }
        return components;
    }
}
class GdxTestBootstrap {
    private static HeadlessApplication app;

    public static void init() {
        if (app == null) {
            HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
            app = new HeadlessApplication(new ApplicationAdapter() {}, config);
        }
    }
}