package com.example.client.modules;

/**
 * Marker module: while on, a mixin (not yet wired) should let your camera look around
 * independently of your body's facing/movement direction. On its own this toggle does nothing
 * visible yet - it needs a render-pipeline hook that's easiest to get right once we're compiling
 * against real 1.21.11 mappings. Flag it here and I'll wire the mixin once the base build works.
 */
public class Freelook extends Module {
    public Freelook() { super("Freelook", Category.BASE); }
}
