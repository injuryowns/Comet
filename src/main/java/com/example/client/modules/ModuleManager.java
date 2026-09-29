package com.example.client.modules;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ModuleManager {
    public final List<Module> modules = new ArrayList<>();
    private final Map<Module.Category, List<Module>> byCategory = new EnumMap<>(Module.Category.class);

    public ModuleManager() {
        for (Module.Category c : Module.Category.values()) byCategory.put(c, new ArrayList<>());

        // Combat
        add(new Triggerbot());
        add(new AutoTotem());
        add(new CrystalMacro());
        add(new AnchorMacro());
        add(new SafeAnchor());
        add(new ShieldBreaker());
        add(new MaceMacro());

        // Movement
        add(new Sprint());

        // Visuals
        add(new Fullbright());
        add(new NoRender());
        add(new JumpCircle());
        add(new Cursor());
        add(new FloatingCubes());
        add(new Gradient());
        add(new Hands());
        add(new SwingAnimation());
        add(new Trajectories());
        add(new TargetEsp());
        add(new Particles());

        // Base / utility
        add(new SusChunk());
        add(new ChunkFinder());
        add(new StorageEsp());
        add(new BlockEsp());
        add(new HoleEsp());
        add(new RelogMethod());
        add(new Freecam());
        add(new Freelook());
    }

    private void add(Module module) {
        modules.add(module);
        byCategory.get(module.category).add(module);
    }

    public List<Module> in(Module.Category category) {
        return byCategory.get(category);
    }

    public <T extends Module> T get(Class<T> type) {
        for (Module m : modules) {
            if (type.isInstance(m)) return type.cast(m);
        }
        return null;
    }

    public boolean isOn(Class<? extends Module> type) {
        Module m = get(type);
        return m != null && m.isEnabled();
    }
}
