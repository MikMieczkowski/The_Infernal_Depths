package com.mikm.rendering.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SequentialSound {
    private List<String> soundNames;
    private long lastTimePlayed;
    private int i = 0;

    public SequentialSound(String startsWith) {
        soundNames = getFilesStartingWith("sound/effects", startsWith);
    }

    void play() {
        if (System.currentTimeMillis() - lastTimePlayed > 2000) {
            i = 0;
        }
        lastTimePlayed = System.currentTimeMillis();
        SoundEffects.play(soundNames.get(i), 1);
        i++;
        if (i >= soundNames.size()) {
            i = 0;
        }
    }

    private ArrayList<String> getFilesStartingWith(String folderPath, String startsWith) {
        ArrayList<String> result = new ArrayList<>();

        FileHandle folder = Gdx.files.internal(folderPath);
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Provided path is not a directory");
        }

        for (FileHandle file : folder.list()) {
            if (file.isDirectory()) continue;

            String name = file.name();
            if (name.startsWith(startsWith)) {
                result.add(name);
            }
        }

        return result;
    }

}
