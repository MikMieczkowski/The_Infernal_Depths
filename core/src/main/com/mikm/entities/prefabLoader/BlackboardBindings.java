package com.mikm.entities.prefabLoader;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.mikm.utils.RandomUtils;
import com.mikm._components.Transform;
import com.mikm._components.routine.RoutineListComponent;
import com.mikm._systems.WorldCollisionMovementSystem;
import com.mikm.entities.actions.Action;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.BlacksmithScreen;

import java.util.Map;

public class BlackboardBindings {
    private static Map<String, Action> nameToBehaviour;

    public static void setNameToBehaviour(Map<String, Action> nameToBehaviour) {
        BlackboardBindings.nameToBehaviour = nameToBehaviour;
    }

    BlackboardBindings() {

        Blackboard.getInstance().bindFunction("inRadiusOfPlayer", (entity, args) -> {
            Transform transform = Transform.MAPPER.get(entity);
            int radius = Integer.parseInt(args.get(0));
            return Intersector.overlaps(
                    new Circle(transform.x, transform.y, radius),
                    Application.getInstance().getPlayerHitbox()
            );
        });

        //Blackboard.getInstance().bind("currentScreen", e->Application.getInstance().currentScreen.getClass().getSimpleName());
        Blackboard.getInstance().bind("currentScreen", e->"BlacksmithScreen");
        Blackboard.getInstance().bind("showMenu", e-> BlacksmithScreen.showMenu);



        Blackboard.getInstance().bindFunction("finished", (e, args) ->
                {
                    RoutineListComponent routineListComponent = RoutineListComponent.MAPPER.get(e);
                    return routineListComponent.inAction(args.get(0));
                }
        );
        Blackboard.getInstance().bindFunction("random", (entity, args) -> {
            float arg = Float.parseFloat(args.get(0));
            return RandomUtils.getPercentage((int) (arg * 100));
        });

        for (String inputAction: GameInput.inputActions) {
            Blackboard.getInstance().bind("pressed" + inputAction, e -> GameInput.isActionPressed(inputAction));
            Blackboard.getInstance().bind("justPressed" + inputAction, e -> GameInput.isActionJustPressed(inputAction));
        }

        Blackboard.getInstance().bind("canFall", WorldCollisionMovementSystem::checkHolePositions);
        Blackboard.getInstance().bind("PI", e->MathUtils.PI);
        Blackboard.getInstance().bind("isMoving", e->GameInput.isMoving());
        //TODO after weaponsystem added
//        Blackboard.getInstance().bind("bowReleased", e-> {
//            if (!e.NAME.equals("player")) {
//                throw new RuntimeException("bowReleased must be called with a player as arg");
//            }
//            Player p = (Player)e;
//            return p.currentHeldItem instanceof Bow && ((Bow)p.currentHeldItem).isReleased();
//        });


        Blackboard.getInstance().bind("bowReleased", e->false);
    }


}
