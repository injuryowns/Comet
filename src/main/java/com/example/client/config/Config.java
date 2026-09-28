package com.example.client.config;

import com.example.client.Client;
import com.example.client.Settings;
import com.example.client.modules.Module;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A named snapshot of everything the GUI settings tab controls (accent color, HUD toggles, name
 * spoof) plus which modules and modes were enabled. Comet's equivalent of Meteor's "profiles".
 */
public final class Config {
    private static final String EXT = ".comet.properties";

    private Config() {}

    private static Path dir() {
        Path d = FabricLoader.getInstance().getConfigDir().resolve("comet/configs");
        try { Files.createDirectories(d); } catch (IOException ignored) {}
        return d;
    }

    public static List<String> list() {
        try (Stream<Path> s = Files.list(dir())) {
            return s.map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(EXT))
                    .map(n -> n.substring(0, n.length() - EXT.length()))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) { return List.of(); }
    }

    public static void save(String name) {
        Properties p = new Properties();
        p.setProperty("r", "" + Settings.r);
        p.setProperty("g", "" + Settings.g);
        p.setProperty("b", "" + Settings.b);
        p.setProperty("nameProtect", "" + Settings.nameProtect);
        p.setProperty("fakeName", Settings.fakeName);
        p.setProperty("arraylist", "" + Settings.arraylist);
        p.setProperty("dimBackground", "" + Settings.dimBackground);

        StringBuilder on = new StringBuilder(), modes = new StringBuilder();
        for (Module m : Client.MODULES.modules) {
            if (m.isEnabled()) on.append(m.name).append(';');
            if (m.mode != 0) modes.append(m.name).append('=').append(m.mode).append(';');
        }
        p.setProperty("enabled", on.toString());
        p.setProperty("modes", modes.toString());

        try (OutputStream out = Files.newOutputStream(dir().resolve(name + EXT))) {
            p.store(out, "Comet config: " + name);
        } catch (IOException ignored) {}
    }

    public static boolean load(String name) {
        Path f = dir().resolve(name + EXT);
        if (!Files.exists(f)) return false;
        try (InputStream in = Files.newInputStream(f)) {
            Properties p = new Properties();
            p.load(in);
            Settings.r = clamp(p.getProperty("r", "" + Settings.r));
            Settings.g = clamp(p.getProperty("g", "" + Settings.g));
            Settings.b = clamp(p.getProperty("b", "" + Settings.b));
            Settings.nameProtect = Boolean.parseBoolean(p.getProperty("nameProtect", "false"));
            Settings.fakeName = p.getProperty("fakeName", Settings.fakeName);
            Settings.arraylist = Boolean.parseBoolean(p.getProperty("arraylist", "true"));
            Settings.dimBackground = Boolean.parseBoolean(p.getProperty("dimBackground", "true"));

            Set<String> on = new HashSet<>(Arrays.asList(p.getProperty("enabled", "").split(";")));
            Map<String, Integer> modes = new HashMap<>();
            for (String pair : p.getProperty("modes", "").split(";")) {
                if (pair.isBlank()) continue;
                String[] kv = pair.split("=");
                if (kv.length == 2) modes.put(kv[0], Integer.parseInt(kv[1]));
            }
            for (Module m : Client.MODULES.modules) {
                if (on.contains(m.name) != m.isEnabled()) m.toggle();
                Integer md = modes.get(m.name);
                if (md != null && m.modes.length > 0) m.mode = Math.min(md, m.modes.length - 1);
            }
            return true;
        } catch (IOException | NumberFormatException e) { return false; }
    }

    public static void delete(String name) {
        try { Files.deleteIfExists(dir().resolve(name + EXT)); } catch (IOException ignored) {}
    }

    private static int clamp(String v) {
        try { return Math.max(0, Math.min(255, Integer.parseInt(v))); } catch (NumberFormatException e) { return 255; }
    }
}
