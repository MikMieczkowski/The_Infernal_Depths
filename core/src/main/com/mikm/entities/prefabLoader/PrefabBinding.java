package com.mikm.entities.prefabLoader;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.Vector2Int;
import com.mikm._components.*;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm.rendering.screens.Application;
import com.mikm.utils.Assets;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class PrefabBinding {
    //Move to PrefabInstantiator and create addEntity methods for all types of entities
    PrefabBinding(Map<String, PrefabInstantiator.EntityInitFunction> entityNameToActionsInit) {
        //EntityLoader entities
        for (String entityName : EntityYAMLReader.getEntitiesInYamlFolder()) {
            EntityYAMLReader.addPrefab(entityName, entityNameToActionsInit);
        }

        //Rope
        Set<Component> components = new HashSet<>();
        Transform t = new Transform();
        t.ENTITY_NAME = "rope";
        components.add(t);

        TriggerComponent triggerComponent = new TriggerComponent(Application.TILE_WIDTH, TriggerEntityType.OTHER, "TALK",
                Event.ON_ENTER, TriggerAction.displayIndicator(),
                Event.ON_STAY_AND_INPUT_JUST_PRESSED, TriggerAction.decreaseCaveFloor()
        );
        /*TODO I think here you were refactoring the trigger component to have a map from Event to triggerAction instead of the three actions
          because you were going to add the onProjectile methods in order to implement mining again and collision. Then you were also going to merge
          trigger and combat system since theyre both kinda relying on this collision table thing and then you should add back rocks and destructibles next
          and then you can finish the particles system and then youl be done with the ECS integration and you can optionally refactor memento. 

         */
        components.add(triggerComponent);

        components.add(new SpriteComponent(Assets.getInstance().getTextureRegion("rope")));
        PrefabInstantiator.addPrefab("rope", components);

        //Door, has special constructor
        components = new HashSet<>();
        t = new Transform();
        t.ENTITY_NAME = "door";
        components.add(t);
        components.add(new TriggerComponent(Application.TILE_WIDTH, TriggerEntityType.OTHER, "TALK", Event.ON_ENTER, TriggerAction.goToScreen()));
        PrefabInstantiator.addPrefab("door", components);

        //NPC
        final int NPC_TALKING_DIAMETER = 48;
        components = new HashSet<>();
        t = new Transform();
        t.ENTITY_NAME = "npc";
        components.add(t);
        components.add(new TriggerComponent(NPC_TALKING_DIAMETER, TriggerEntityType.OTHER, "TALK",
                Event.ON_STAY, TriggerAction.displayIndicator(),
                Event.ON_STAY_AND_INPUT_JUST_PRESSED, TriggerAction.npcTalk()));
        components.add(new SpriteComponent());
        t.FULL_BOUNDS_DIMENSIONS = new Vector2Int(32, 32);
        PrefabInstantiator.addPrefab("npc", components);

        //Grave
        final int GRAVE_COLLECT_DIAMETER = 72;
        components = new HashSet<>();
        t = new Transform();
        t.ENTITY_NAME = "grave";
        components.add(t);
        components.add(new TriggerComponent(GRAVE_COLLECT_DIAMETER, TriggerEntityType.OTHER, "TALK",
                Event.ON_ENTER, TriggerAction.graveCollect()));
        final TextureRegion grave = Assets.getInstance().getTextureRegion("grave");
        components.add(new SpriteComponent(grave));
        components.add(new GraveComponent());
        PrefabInstantiator.addPrefab("grave", components);

        //Rock - only breakable by mining projectiles
        components = new HashSet<>();
        t = new Transform();
        t.ENTITY_NAME = "rock";
        components.add(t);
        components.add(new TriggerComponent(Application.TILE_WIDTH, TriggerEntityType.ROCK,
                Event.ON_PLAYER_MINING_PROJECTILE_STAY, TriggerAction.breakRock()
        ));
        components.add(new SpriteComponent());
        components.add(new RockComponent());
        PrefabInstantiator.addPrefab("rock", components);

        //Destructible - breakable by any player projectile (grass, pots, etc)
        components = new HashSet<>();
        t = new Transform();
        t.ENTITY_NAME = "destructible";
        components.add(t);
        components.add(new TriggerComponent(Application.TILE_WIDTH, TriggerEntityType.DESTRUCTIBLE,
                Event.ON_PLAYER_PROJECTILE_STAY, TriggerAction.breakDestructible(),
                Event.ON_PLAYER_MINING_PROJECTILE_STAY, TriggerAction.breakDestructible()
        ));
        components.add(new SpriteComponent());
        components.add(new DestructibleComponent());
        PrefabInstantiator.addPrefab("destructible", components);

        //Particle
        components = new HashSet<>();
        t = new Transform();
        t.Z_ORDER = 3;
        t.ENTITY_NAME = "particle";
        components.add(t);
        components.add(new SpriteComponent());
        components.add(new EffectsComponent());
        WorldColliderComponent c = new WorldColliderComponent();
        c.HITBOX_OFFSETS = new Vector2Int();
        components.add(c);
        components.add(new RoutineListComponent());
        PrefabInstantiator.addPrefab("particle", components);

        //Projectile
        components = new HashSet<>();
        t = new Transform();
        t.ENTITY_NAME = "projectile";
        components.add(t);
        components.add(new SpriteComponent());
        components.add(new RoutineListComponent());
        PrefabInstantiator.addPrefab("projectile", components);

        //PlayerWeapon
        components = new HashSet<>();
        t = new Transform();
        t.ENTITY_NAME = "playerWeapon";
        components.add(t);
        components.add(new SpriteComponent());
        components.add(new RoutineListComponent());
        PrefabInstantiator.addPrefab("playerWeapon", components);

        //Test Object, has special constructor
        components = new HashSet<>();
        t = new Transform();
        t.ENTITY_NAME = "test";
        components.add(t);
        components.add(new SpriteComponent(Assets.testTexture));
        PrefabInstantiator.addPrefab("testObject", components);

        //Decoration - static sprites rendered in front of entities
        components = new HashSet<>();
        t = new Transform();
        t.ENTITY_NAME = "decoration";
        t.Z_ORDER = 1;
        components.add(t);
        components.add(new SpriteComponent());
        PrefabInstantiator.addPrefab("decoration", components);

    }
}
