package com.example.client.modules;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        // Combat
        modules.add(new Triggerbot());
        modules.add(new AutoTotem());
        modules.add(new CrystalMacro());
        modules.add(new AnchorMacro());
        modules.add(new SafeAnchor());
        modules.add(new ShieldBreaker());
        modules.add(new MaceMacro());
        // Movement
        modules.add(new Sprint());
        // Visuals
        modules.add(new Fullbright());
        modules.add(new NoRender());
        modules.add(new JumpCircle());
        modules.add(new Cursor());
        modules.add(new FloatingCubes());
        modules.add(new Gradient());
        modules.add(new Hands());
        modules.add(new SwingAnimation());
        modules.add(new Trajectories());
        modules.add(new TargetEsp());
        modules.add(new Particles());
        // Base
        modules.add(new SusChunk());
        modules.add(new ChunkFinder());
        modules.add(new StorageEsp());
        modules.add(new BlockEsp());
        modules.add(new HoleEsp());
        modules.add(new RelogMethod());
        modules.add(new Freecam());
        modules.add(new Freelook());
    }

    public List<Module> in(Module.Category c) {
        return modules.stream().filter(m -> m.category == c).toList();
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> type) {
        for (Module m : modules) if (type.isInstance(m)) return (T) m;
        return null;
    }

    public boolean isOn(Class<? extends Module> type) {
        for (Module m : modules) if (type.isInstance(m)) return m.isEnabled();
        return false;
    }
}
