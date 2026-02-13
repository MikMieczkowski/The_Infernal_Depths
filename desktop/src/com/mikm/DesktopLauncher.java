package com.mikm;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mikm.rendering.screens.Application;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
    //Set to false when making jar or release version
	private static final boolean PACK = true;
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setWindowedMode(1440, 810);
		config.setTitle("The Infernal Depths");
		if (PACK) {

			String inputDir = "images/source";
			String outputDir = "images";       // output dir
			String atlasName = "The Infernal Depths";                 // atlas name
			com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings settings = new com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings();
			settings.silent = true;
			settings.maxWidth = 2048;  // or 4096 depending on your images
			settings.maxHeight = 2048;
			settings.combineSubdirectories = true; // important to include multiple folders

			com.badlogic.gdx.tools.texturepacker.TexturePacker.process(settings, inputDir, outputDir, atlasName);
		}
		new Lwjgl3Application(Application.getInstance(), config);
	}
}
