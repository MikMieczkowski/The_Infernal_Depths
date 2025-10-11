package com.mikm.rendering.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;
import java.util.Map;

public class SoundEffects {
    private SoundEffects() {

    }
    public static float SFX_VOLUME = 1;

    private static Map<String, Sound> nameToSound = new HashMap<>();
    private static Map<String, SequentialSound> sequentialByPrefix = new HashMap<>();
    private static Map<String, SoundInstance> loopNameToSound = new HashMap<>();

    private static class SoundInstance {
        Sound sound;
        long id;
        float vol;

        public SoundInstance(Sound sound, long id, float vol) {
            this.sound = sound;
            this.id = id;
            this.vol = vol;
        }
    }

    static {
        // Hardcoded sequential groups by startsWith
        sequentialByPrefix.put("blacksmith", new SequentialSound("blacksmith")); // blacksmith1..7
        sequentialByPrefix.put("hammer", new SequentialSound("hammer"));         // hammer1..3
        sequentialByPrefix.put("playerDive", new SequentialSound("playerDive")); // playerDive1..4
        sequentialByPrefix.put("playerHit", new SequentialSound("playerHit"));   // playerHit1..4
        sequentialByPrefix.put("swing", new SequentialSound("swing"));   // playerHit1..4
    }

    public static void playLoop(String name, float mul) {
        SoundInstance instance = play(name, mul, true);
        loopNameToSound.put(name, instance);
    }
    public static void playLoop(String name) {
        playLoop(name, 1);
    }

    public static boolean loopIsPlaying(String name) {
        return loopNameToSound.containsKey(name);
    }

    public static void updateLoopVolumes() {
        for (Map.Entry<String, SoundInstance> entry : loopNameToSound.entrySet()) {
            SoundInstance instance = entry.getValue();
            instance.sound.setVolume(instance.id, instance.vol *SFX_VOLUME);
        }
    }

    public static void setLoopVolume(String name, float vol) {
        SoundInstance instance = loopNameToSound.get(name);
        instance.vol = vol;
        instance.sound.setVolume(instance.id, vol*SFX_VOLUME);
    }

    public static void stopLoop(String name) {
        SoundInstance instance = loopNameToSound.get(name);
        instance.sound.stop(instance.id);
        nameToSound.remove(name);
    }

    public static void playLoud(String name) {
        play(name, 2);
    }
    public static void playQuiet(String name) {
        play(name, 0.3f);
    }

    public static void play(String name) {
        // Route base names to sequential groups when configured
        if (name == null) {
            return;
        }
        String base = name;
        if (base.endsWith(".ogg")) {
            base = base.substring(0, base.length()-4);
        }
        String key = base.replaceAll("\\d+$", "");
        if (sequentialByPrefix.containsKey(key) && base.equals(key)) {
            sequentialByPrefix.get(key).play();
            return;
        }
        play(name, 1);
    }

    static void play(String name, float mul) {
        play(name, mul, false);
    }
    private static SoundInstance play(String name, float mul, boolean loop) {
        if (name == null) {
            return null;
        }
        Sound sound = loadOrGet(name);
        if (loop) {
            long id = sound.loop();
            return new SoundInstance(sound, id, mul * SFX_VOLUME);
        }
        if (name.equals("step")) {
            sound.play(1.5f*mul* SFX_VOLUME);
        } else {
            sound.play(mul* SFX_VOLUME);
        }
        return null;
    }

    static Sound loadOrGet(String name) {
        if (nameToSound.containsKey(name)) {
            Sound sound = nameToSound.get(name);
            if (sound == null) {
                return null;
            }
            return sound;
        } else {
            Sound sound = Gdx.audio.newSound(Gdx.files.internal("sound/effects/" + name));
            if (sound == null) {
                throw new RuntimeException("Couldn't load sound" + name);
            }
            nameToSound.put(name, sound);
            return sound;
        }
    }

    public static void dispose() {
        for (Map.Entry<String, Sound> entry : nameToSound.entrySet()) {
            entry.getValue().dispose();
        }
        for (Map.Entry<String, SoundInstance> entry : loopNameToSound.entrySet()) {
            entry.getValue().sound.dispose();
        }
        //don't need to dipose sequentialSounds
    }
}
