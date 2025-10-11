package com.mikm.entityLoader;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.mikm.RandomUtils;
import com.mikm.Vector2Int;
import com.mikm.entities.Entity;
import com.mikm.entities.actions.Action;
import com.mikm.entities.player.Player;
import com.mikm.entities.player.weapons.Bow;
import com.mikm.input.GameInput;
import com.mikm.rendering.screens.Application;
import com.mikm.rendering.screens.BlacksmithScreen;

import java.util.ArrayList;
import java.util.Map;

public class BlackboardBindings {
    private static Map<String, Action> nameToBehaviour;

    public static void setNameToBehaviour(Map<String, Action> nameToBehaviour) {
        BlackboardBindings.nameToBehaviour = nameToBehaviour;
    }

    BlackboardBindings() {
        Blackboard.getInstance().bindFunction("inRadiusOfPlayer", (entity, args) -> {
            int radius = Integer.parseInt(args.get(0));
            return Intersector.overlaps(
                    new Circle(entity.x, entity.y, radius),
                    Application.player.getHitbox()
            );
        });

        //Blackboard.getInstance().bind("currentScreen", e->Application.getInstance().currentScreen.getClass().getSimpleName());
        Blackboard.getInstance().bind("currentScreen", e->"BlacksmithScreen");
        Blackboard.getInstance().bind("showMenu", e-> BlacksmithScreen.showMenu);



        Blackboard.getInstance().bindFunction("finished", (e, args) ->
                e.routineHandler.inAction(nameToBehaviour.get(args.get(0)).name));
        Blackboard.getInstance().bindFunction("random", (entity, args) -> {
            float arg = Float.parseFloat(args.get(0));
            return RandomUtils.getPercentage((int) (arg * 100));
        });

        for (String inputAction: GameInput.inputActions) {
            Blackboard.getInstance().bind("pressed" + inputAction, e -> GameInput.isActionPressed(inputAction));
            Blackboard.getInstance().bind("justPressed" + inputAction, e -> GameInput.isActionJustPressed(inputAction));
        }

        Blackboard.getInstance().bind("canFall", this::checkHolePositions);
        Blackboard.getInstance().bind("PI", e->MathUtils.PI);
        Blackboard.getInstance().bind("isMoving", e->GameInput.isMoving());
        Blackboard.getInstance().bind("bowReleased", e-> {
            if (!e.NAME.equals("player")) {
                throw new RuntimeException("bowReleased must be called with a player as arg");
            }
            Player p = (Player)e;
            return p.currentHeldItem instanceof Bow && ((Bow)p.currentHeldItem).isReleased();
        });

    }

    private boolean checkHolePositions(Entity entity) {
        if (!entity.NAME.equals("player")) {
            throw new RuntimeException("canFall must be called with player as arg");
        }

        if (!entity.routineHandler.inAction("Fall") && !entity.routineHandler.inAction("Dive")) {
            boolean[][] holePositions;
            if (Application.getInstance().currentScreen == Application.getInstance().caveScreen) {
                holePositions = Application.getInstance().caveScreen.getHolePositionsToCheck();
            } else if (Application.getInstance().currentScreen == Application.getInstance().townScreen) {
                holePositions = Application.getInstance().townScreen.getHolePositions();
            } else if (Application.getInstance().currentScreen == Application.getInstance().slimeBossRoomScreen) {
                holePositions = Application.getInstance().slimeBossRoomScreen.getHolePositions();
            } else {
                return false;
            }
            ArrayList<Vector2Int> wallTilesToCheck = entity.collider.getWallTilePositionsToCheck();
            boolean aboveHole = false;
            for (Vector2Int checkedWallTilePosition : wallTilesToCheck) {
                int x = 0, y;
                for (y = -1; y <= 1; y += 1) {
                    aboveHole = aboveHole || checkTile(entity, checkedWallTilePosition, holePositions, x, y);
                }
                y=0;
                for (x = -1; x <= 1; x += 1) {
                    aboveHole = aboveHole || checkTile(entity, checkedWallTilePosition, holePositions, x, y);
                }
            }
            return aboveHole;
        }
        return false;
    }

    private boolean checkTile(Entity entity, Vector2Int checkedWallTilePosition, boolean[][] holePositions, int x, int y) {
        Vector2Int v = new Vector2Int(checkedWallTilePosition.x + x, checkedWallTilePosition.y + y);
        Rectangle checkedTileBounds = new Rectangle(v.x * Application.TILE_WIDTH, v.y * Application.TILE_HEIGHT, Application.TILE_WIDTH, Application.TILE_HEIGHT);
        boolean isInBounds = v.x >= 0 && v.x < holePositions[0].length && v.y >= 0 && v.y < holePositions.length;
        boolean vIsHole = false;
        try {
            vIsHole = holePositions[v.y][v.x];
        } catch (Exception e) {

        }
        boolean output = false;
        if (isInBounds && vIsHole) {
            if (checkedTileBounds.contains(entity.getHitbox().x, entity.getHitbox().y)) {
                output = true;
            }
        }
        return output;
    }
}
