package com.mikm.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;
import java.util.Map;

public class SoundEffects {
    private SoundEffects() {

    }
    public static float SFX_VOLUME = 1;


    //TODO Delete all of these
    public static Sound step;
    public static Sound dash;
    public static Sound hit;
    public static Sound playerHit;
    private static Sound swing1;
    private static Sound swing2;
    private static Sound swing3;
    public static Sound[] swing;
    public static Sound reward, rockBreak, ropeClimb, slimeJump, slimeLand, batHit, slimeHit, potBreak, grassBreak;
    public static Sound bowImpact, bowReady, bowShoot, menuDeny;

    private static Map<String, Sound> nameToSound = new HashMap<>();

    public static void create() {
        //To remove
        menuDeny = Gdx.audio.newSound(Gdx.files.internal("sound/effects/menuDeny.ogg"));
        bowImpact = Gdx.audio.newSound(Gdx.files.internal("sound/effects/bowImpact.ogg"));
        bowReady = Gdx.audio.newSound(Gdx.files.internal("sound/effects/bowReady.ogg"));
        bowShoot = Gdx.audio.newSound(Gdx.files.internal("sound/effects/bowShoot.ogg"));
        potBreak = Gdx.audio.newSound(Gdx.files.internal("sound/effects/potBreak.ogg"));
        grassBreak = Gdx.audio.newSound(Gdx.files.internal("sound/effects/grassBreak.ogg"));
        reward = Gdx.audio.newSound(Gdx.files.internal("sound/effects/reward.ogg"));
        rockBreak = Gdx.audio.newSound(Gdx.files.internal("sound/effects/rockBreak.ogg"));
        ropeClimb = Gdx.audio.newSound(Gdx.files.internal("sound/effects/ropeClimb.ogg"));
        slimeJump = Gdx.audio.newSound(Gdx.files.internal("sound/effects/slimeJump.ogg")); //
        slimeLand = Gdx.audio.newSound(Gdx.files.internal("sound/effects/slimeLand.ogg")); //
        slimeHit = Gdx.audio.newSound(Gdx.files.internal("sound/effects/slimeHit.ogg")); //
        batHit = Gdx.audio.newSound(Gdx.files.internal("sound/effects/batHit.ogg")); //
        step = Gdx.audio.newSound(Gdx.files.internal("sound/effects/step.ogg"));
        dash = Gdx.audio.newSound(Gdx.files.internal("sound/effects/dash.ogg"));
        hit = Gdx.audio.newSound(Gdx.files.internal("sound/effects/hit.ogg"));
        playerHit = Gdx.audio.newSound(Gdx.files.internal("sound/effects/playerHit.ogg")); //
        swing1 = Gdx.audio.newSound(Gdx.files.internal("sound/effects/swing1.ogg"));
        swing2 = Gdx.audio.newSound(Gdx.files.internal("sound/effects/swing2.ogg"));
        swing3 = Gdx.audio.newSound(Gdx.files.internal("sound/effects/swing3.ogg"));
        swing = new Sound[]{swing1, swing2, swing3};
    }

    public static Sound load(String name) {
        return Gdx.audio.newSound(Gdx.files.internal("sound/effects/" + name + ".ogg"));
    }

    public static void playLoud(Sound sound) {
        play(sound, 2 * SFX_VOLUME);
    }

    public static void play(String name) {
        play(name, 1);
    }

    private static void play(String name, float mul) {
        Sound sound;
        if (nameToSound.containsKey(name)) {
            sound = nameToSound.get(name);
            if (sound == null) {
                return;
            }
        } else {
            sound = load(name);
        }
        if (sound == step) {
            sound.play(1.5f*mul* SFX_VOLUME);
        } else {
            sound.play(mul* SFX_VOLUME);
        }
    }

    public static void playQuiet(Sound sound) {
        play(sound, 0.3f* SFX_VOLUME);
    }



    public static void dispose() {
        step.dispose();
        dash.dispose();
        hit.dispose();
        swing1.dispose();
        swing2.dispose();
        swing3.dispose();
        reward.dispose();
        rockBreak.dispose();
        ropeClimb.dispose();
        slimeJump.dispose();
        batHit.dispose();
        slimeHit.dispose();
        potBreak.dispose();
        grassBreak.dispose();
        bowImpact.dispose();
        bowReady.dispose();
        bowShoot.dispose();
        menuDeny.dispose();
    }
}
