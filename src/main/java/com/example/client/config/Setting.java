package com.example.client.config;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** A single tunable value a module exposes to the right-click settings panel. */
public abstract class Setting<T> {
    public final String name;
    protected final Supplier<T> getter;
    protected final Consumer<T> setter;

    protected Setting(String name, Supplier<T> getter, Consumer<T> setter) {
        this.name = name; this.getter = getter; this.setter = setter;
    }

    public T get() { return getter.get(); }
    public void set(T v) { setter.accept(v); }
    public abstract String display();

    public static class IntSetting extends Setting<Integer> {
        public final int min, max;
        public IntSetting(String name, Supplier<Integer> g, Consumer<Integer> s, int min, int max) {
            super(name, g, s); this.min = min; this.max = max;
        }
        public void setFraction(double f) { set((int) Math.round(min + (max - min) * Math.max(0, Math.min(1, f)))); }
        public double fraction() {
            if (max <= min) return 0.0;
            return Math.max(0.0, Math.min(1.0, (get() - min) / (double) (max - min)));
        }
        @Override public String display() { return name + ": " + get(); }
    }

    public static class BoolSetting extends Setting<Boolean> {
        public BoolSetting(String name, Supplier<Boolean> g, Consumer<Boolean> s) { super(name, g, s); }
        public void toggle() { set(!get()); }
        @Override public String display() { return name; }
    }
}
