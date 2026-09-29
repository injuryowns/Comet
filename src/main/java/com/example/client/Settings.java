package com.example.client;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Global client settings, persisted to config/comet.properties. */
public final class Settings {
    public static int r = 43, g = 217, b = 138;      // accent color
    public static boolean nameProtect = false;       // client-side display name replacement
    public static String fakeName = "Player";
    public static boolean arraylist = true;
    public static boolean dimBackground = true;

    private Settings() {}

    public static int accent() { return 0xFF000000 | (r << 16) | (g << 8) | b; }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("comet.properties");
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try (InputStream in = Files.newInputStream(f)) {
            Properties p = new Properties();
            p.load(in);
            r = clamp(Integer.parseInt(p.getProperty("r", "" + r)));
            g = clamp(Integer.parseInt(p.getProperty("g", "" + g)));
            b = clamp(Integer.parseInt(p.getProperty("b", "" + b)));
            nameProtect = Boolean.parseBoolean(p.getProperty("nameProtect", "" + nameProtect));
            fakeName = p.getProperty("fakeName", fakeName);
            arraylist = Boolean.parseBoolean(p.getProperty("arraylist", "" + arraylist));
            dimBackground = Boolean.parseBoolean(p.getProperty("dimBackground", "" + dimBackground));
        } catch (IOException | NumberFormatException ignored) {}
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("r", "" + r);
        p.setProperty("g", "" + g);
        p.setProperty("b", "" + b);
        p.setProperty("nameProtect", "" + nameProtect);
        p.setProperty("fakeName", fakeName);
        p.setProperty("arraylist", "" + arraylist);
        p.setProperty("dimBackground", "" + dimBackground);
        try {
            Files.createDirectories(file().getParent());
        } catch (IOException ignored) {}
        try (OutputStream out = Files.newOutputStream(file())) {
            p.store(out, "Client settings");
        } catch (IOException ignored) {}
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
}
