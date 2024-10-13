package com.mikm.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class SoundEffects {
    private SoundEffects() {

    }
    public static Sound step;
    public static Sound dash;
    public static Sound hit;
    public static Sound playerHit;
    private static Sound swing1;
    private static Sound swing2;
    private static Sound swing3;
    public static Sound[] swing;

    public static void create() {
        step = Gdx.audio.newSound(Gdx.files.internal("sound/effects/step.ogg"));
        dash = Gdx.audio.newSound(Gdx.files.internal("sound/effects/dash.ogg"));
        hit = Gdx.audio.newSound(Gdx.files.internal("sound/effects/hit.ogg"));
        playerHit = Gdx.audio.newSound(Gdx.files.internal("sound/effects/playerHit.ogg"));
        swing1 = Gdx.audio.newSound(Gdx.files.internal("sound/effects/swing1.ogg"));
        swing2 = Gdx.audio.newSound(Gdx.files.internal("sound/effects/swing2.ogg"));
        swing3 = Gdx.audio.newSound(Gdx.files.internal("sound/effects/swing3.ogg"));
        swing = new Sound[]{swing1, swing2, swing3};
    }
    public static long playLoud(Sound sound) {
        return play(sound, 2);
    }
    public static long play(Sound sound) {
        return play(sound, 1);
    }
    private static long play(Sound sound, int mul) {
        if (sound == step) {
            return sound.play(1.5f*mul);
        } else {
            return sound.play(mul);
        }
    }

    public static void dispose() {
        step.dispose();
        dash.dispose();
        hit.dispose();
        swing1.dispose();
        swing2.dispose();
        swing3.dispose();
    }
}
