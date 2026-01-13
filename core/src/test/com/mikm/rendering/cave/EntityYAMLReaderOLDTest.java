package com.mikm.rendering.cave;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.mikm.entities.entityLoaderOld.EntityLoaderOLD;
import com.mikm.entities.player.Player;
import com.mikm.rendering.screens.Application;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class GdxTestBootstrap {
    private static HeadlessApplication app;

    public static void init() {
        if (app == null) {
            HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
            app = new HeadlessApplication(new ApplicationAdapter() {}, config);
        }
    }
}
class EntityYAMLReaderOLDTest {
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
    public void test() {
        Entity p = Application.playerOLD = (Player) EntityLoaderOLD.create("player");
        Entity e = EntityLoaderOLD.create("slime");

        System.out.println(p.NAME);
        assert Objects.equals(p.NAME, "player");
        assert Objects.equals(e.NAME, "slime");
    }

    @Test
    public void test2() {
        Entity e = EntityLoaderOLD.create("rat");
        assert e.NAME.equals("rat");
        assert e.routineHandler.inAction("Idle");
    }

    @Test
    public void test3() {
        Entity e = EntityLoaderOLD.create("rat");
        Entity e2 = EntityLoaderOLD.create("rat");
        assert e != e2;
        assert e.NAME.equals(e2.NAME);
    }

    @Test
    public void test4() {
        if (Application.playerOLD == null) {
            Application.playerOLD = (Player) EntityLoaderOLD.create("player");
        }
        EntityLoaderOLD.create("slime");
        EntityLoaderOLD.create("bat");
        Entity e = EntityLoaderOLD.create("moti");
        assert e.NAME.equals("moti");
    }


    @Test
    public void test6() {
        assertThrows(Exception.class, () -> EntityLoaderOLD.create("nonsense"));
    }

    public void test5() {
        a("empty");
        a("test1");
        a("test2");
        a("test3");
        a("test4");
        a("test5");
        a("test6");
        a("test7");
        a("test8");
        a("test9");
        a("test10");
        a("test11");
        a("test12");
        //valid, shouldn't throw error
        EntityLoaderOLD.create("test/" + "test14");


        //minimal entity. test defaults
        Entity e = EntityLoaderOLD.create("test/" + "test15");
        assert e.NAME.equals("test2");
        assert e.hp == 3;
        assert e.DAMAGE == 1;
        assert e.KNOCKBACK == 1;
        assert e.MAX_HP == 3;
        assert e.SPEED == 1;
        assert e.ORIGIN_X == 0;
        assert e.ORIGIN_Y == 0;
        assert e.isAttackable;
        // removed entity-level POST_HIT_ROUTINE
        assert e.HURT_SOUND_EFFECT == null;
        assert e.spawnProbability == null;
        assert e.HAS_SHADOW;

        assert e.routineHandler.inAction("Idle");


        //minimal entity with ACTION and custom behaviour name
        Entity e1 = EntityLoaderOLD.create("test/" + "test13");
        assert e1.NAME.equals("test2");
        assert e1.routineHandler.inAction("Idle");

        //entity routine deep copying
        Entity e2 = EntityLoaderOLD.create("test/" + "test13");
        e1.xVel = 1;
        assert e1.xVel != e2.xVel;
        assert e1.routineHandler.currentRoutine != e2.routineHandler.currentRoutine;
        assert e1.routineHandler.currentRoutine.cycle.currentAction.name.equals(e2.routineHandler.currentRoutine.cycle.currentAction.name);
        assert e1.routineHandler.currentRoutine.cycle != e2.routineHandler.currentRoutine.cycle;
        assert e1.routineHandler.currentRoutine.cycle.currentAction != e2.routineHandler.currentRoutine.cycle.currentAction;
        e1.routineHandler.currentRoutine.cycle.currentAction.MAX_TIME = 143f;
        e2.routineHandler.currentRoutine.cycle.currentAction.MAX_TIME = 7f;
        assert e1.routineHandler.currentRoutine.cycle.currentAction.MAX_TIME == 143;

    }

    @Test
    public void test7() {
        if (Application.playerOLD == null) {
            Application.playerOLD = (Player) EntityLoaderOLD.create("player");
        }
        Entity e = EntityLoaderOLD.create("test/test16");
        Application.playerOLD.x = 0;
        Application.playerOLD.y = 0;
        e.x = 0;
        e.y = 100;
        assert e.routineHandler.currentRoutine.transitions.conditionTransitions.get(0).getCondition(e);
        e.x = 0;
        e.y = 300;
        assert !e.routineHandler.currentRoutine.transitions.conditionTransitions.get(0).getCondition(e);

        System.out.println(Application.playerOLD.routineHandler.currentRoutine.cycle.currentAction.name);
        assert !Application.playerOLD.routineHandler.currentRoutine.transitions.conditionTransitions.get(0).getCondition(Application.playerOLD);
    }


    private void a(String name) {
        boolean assertOn = true;
        if (assertOn) {
            assertThrows(Exception.class, () -> EntityLoaderOLD.create("test/" + name));
        } else {
            EntityLoaderOLD.create("test/" + name);
        }
    }


    @Test
    public void testRoutineHandler() {
        if (Application.playerOLD == null) {
            Application.playerOLD = (Player) EntityLoaderOLD.create("player");
        }
        //moti doesn't even have conditionTransitions?
        Entity e = EntityLoaderOLD.create("test/test14");
        //IS_MOVING condition
        ConditionTransition t = e.routineHandler.currentRoutine.transitions.conditionTransitions.get(0);
        assert !t.getCondition(e);
    }
}
