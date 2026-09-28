package com.example.client.modules;

import com.example.client.Client;

/** Hides the fire, in-wall and underwater screen overlays and the hurt camera tilt (via mixins). */
public class NoRender extends Module {
    public NoRender() { super("No Render", Category.VISUALS); }

    public static boolean active() {
        NoRender m = Client.MODULES.get(NoRender.class);
        return m != null && m.isEnabled();
    }
}
