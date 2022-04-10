package com.h.game;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    static {
        File file = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        if (file.isFile()) {
            file = file.getParentFile();
        }
        byte[] bytes = new byte[12800];
        try {
            Main.class.getResourceAsStream("/resources/dll/Game.dll").read(bytes);
            Files.write(Paths.get(file.getAbsolutePath(), "Game.dll"), bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.load(file + "\\Game.dll");
    }

    public static void main(String[] args) {
        new SnakeGame();
    }
}
