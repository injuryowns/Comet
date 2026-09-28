package com.example.client.gui;

import com.example.client.Client;
import com.example.client.Settings;
import com.example.client.modules.Module;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import com.example.client.modules.Cursor;
import com.example.client.util.ColorUtil;
import org.lwjgl.glfw.GLFW;
import com.example.client.config.Config;
import net.minecraft.client.gui.widget.ButtonWidget;
import java.util.List;
import java.util.ArrayList;

import java.util.List;

/** Two tabs: Modules (category panels) and Settings (accent color, name spoof, toggles). */
public class ClickGui extends Screen {
    private static final int PANEL_W = 120, HEADER_H = 16, ROW_H = 14, GAP = 8, TOP = 40;
    private static final int BG = 0xE6141418, HEADER = 0xFF0D0D10, HOVER = 0x30FFFFFF, OFF = 0xFFB0B0B8;
    private static final int TAB_W = 70, TAB_Y = 14, TAB_H = 16;
    private static final int CFG_W = 160, CFG_ROW = 16;
    private static final int SET_W = 200;
    private static final int[] PRESETS = {0x2BD98A, 0x3D8BFF, 0xA45CFF, 0xFF5CA8, 0xFF4D4D, 0xFF9F1C};

    private int tab = 0;          // 0 = modules, 1 = settings, 2 = configs
    private int dragging = -1;    // which RGB slider is being dragged
    private TextFieldWidget nameField;
    private TextFieldWidget configNameField;
    private final List<String> configs = new ArrayList<>();

    public ClickGui() { super(Text.literal("Click GUI")); }

    @Override
    protected void init() {
        nameField = new TextFieldWidget(textRenderer, setX() + 8, fieldY(), SET_W - 16, 16, Text.literal("Fake name"));
        nameField.setMaxLength(16);
        nameField.setText(Settings.fakeName);
        nameField.setChangedListener(s -> Settings.fakeName = s);
        nameField.setVisible(tab == 1);
        addDrawableChild(nameField);

        configNameField = new TextFieldWidget(textRenderer, cfgX() + 8, TOP + HEADER_H + 6, CFG_W - 16, 16, Text.literal("Config name"));
        configNameField.setMaxLength(24);
        configNameField.setText("default");
        configNameField.setVisible(tab == 2);
        addDrawableChild(configNameField);
        refreshConfigs();
        if (Client.MODULES.isOn(Cursor.class))
            GLFW.glfwSetInputMode(client.getWindow().getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
    }

    // ---- layout helpers -------------------------------------------------
    private int panelX(int index) {
        int total = Module.Category.values().length * (PANEL_W + GAP) - GAP;
        return (width - total) / 2 + index * (PANEL_W + GAP);
    }
    private int setX() { return (width - SET_W) / 2; }
    private int cfgX() { return (width - CFG_W) / 2; }
    private void refreshConfigs() { configs.clear(); configs.addAll(Config.list()); }
    private int presetY() { return TOP + HEADER_H + 6; }
    private int sliderY(int i) { return TOP + HEADER_H + 26 + i * 16; }
    private int toggleY(int i) { return TOP + HEADER_H + 26 + 3 * 16 + 6 + i * ROW_H; }
    private int fieldY() { return toggleY(3) + 4; }
    private int setH() { return fieldY() + 16 + 8 - TOP; }
    private int tabX(int i) { return width / 2 - TAB_W * 3 / 2 + i * TAB_W; }
    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
    private int channel(int i) { return i == 0 ? Settings.r : i == 1 ? Settings.g : Settings.b; }
    private void setChannel(int i, int v) {
        if (i == 0) Settings.r = v; else if (i == 1) Settings.g = v; else Settings.b = v;
    }
    private void dragSlider(double mx) {
        int sx = setX() + 22, sw = SET_W - 30;
        int v = (int) Math.round((mx - sx) / sw * 255);
        setChannel(dragging, Math.max(0, Math.min(255, v)));
    }

    // ---- rendering ------------------------------------------------------
    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        if (Settings.dimBackground) ctx.fill(0, 0, width, height, 0x66000000);
        renderTabs(ctx);
        if (tab == 0) renderModules(ctx, mx, my);
        else if (tab == 1) renderSettings(ctx, mx, my, delta);
        else renderConfigs(ctx, mx, my, delta);
        if (Client.MODULES.isOn(Cursor.class)) Cursor.drawGuiCursor(ctx, mx, my);
    }

    private void renderTabs(DrawContext ctx) {
        ctx.drawTextWithShadow(textRenderer, "Comet", 8, TAB_Y + 4, ColorUtil.accent(0));
        String[] names = {"Modules", "Settings", "Configs"};
        for (int i = 0; i < 3; i++) {
            int x = tabX(i);
            ctx.fill(x, TAB_Y, x + TAB_W, TAB_Y + TAB_H, i == tab ? BG : HEADER);
            if (i == tab) ctx.fill(x, TAB_Y + TAB_H - 1, x + TAB_W, TAB_Y + TAB_H, Settings.accent());
            int tw = textRenderer.getWidth(names[i]);
            ctx.drawTextWithShadow(textRenderer, names[i], x + (TAB_W - tw) / 2, TAB_Y + 4,
                    i == tab ? Settings.accent() : OFF);
        }
    }

    private void renderModules(DrawContext ctx, int mx, int my) {
        int i = 0;
        for (Module.Category cat : Module.Category.values()) {
            int x = panelX(i++), y = TOP;
            List<Module> mods = Client.MODULES.in(cat);
            int h = HEADER_H + Math.max(1, mods.size()) * ROW_H;
            ctx.fill(x, y, x + PANEL_W, y + h, BG);
            ctx.fill(x, y, x + PANEL_W, y + HEADER_H, HEADER);
            ctx.fill(x, y + HEADER_H - 1, x + PANEL_W, y + HEADER_H, ColorUtil.accent(x * 0.004f));
            ctx.drawTextWithShadow(textRenderer, cat.name().charAt(0) + cat.name().substring(1).toLowerCase(),
                    x + 6, y + 4, 0xFFFFFFFF);
            int ry = y + HEADER_H;
            for (Module m : mods) {
                if (in(mx, my, x, ry, PANEL_W, ROW_H)) ctx.fill(x, ry, x + PANEL_W, ry + ROW_H, HOVER);
                int mc = ColorUtil.accent(ry * 0.01f);
                if (m.isEnabled()) ctx.fill(x, ry, x + 2, ry + ROW_H, mc);
                ctx.drawTextWithShadow(textRenderer, m.name, x + 8, ry + 3, m.isEnabled() ? mc : OFF);
                if (m.modes.length > 0) {
                    String ml = m.modeName();
                    ctx.drawTextWithShadow(textRenderer, ml, x + PANEL_W - textRenderer.getWidth(ml) - 5, ry + 3, 0xFF6C6C75);
                }
                ry += ROW_H;
            }
        }
    }

    private void renderSettings(DrawContext ctx, int mx, int my, float delta) {
        int x = setX();
        ctx.fill(x, TOP, x + SET_W, TOP + setH(), BG);
        ctx.fill(x, TOP, x + SET_W, TOP + HEADER_H, HEADER);
        ctx.fill(x, TOP + HEADER_H - 1, x + SET_W, TOP + HEADER_H, Settings.accent());
        ctx.drawTextWithShadow(textRenderer, "Settings", x + 6, TOP + 4, 0xFFFFFFFF);

        // preset swatches
        for (int i = 0; i < PRESETS.length; i++) {
            int sx = x + 8 + i * 22;
            ctx.fill(sx, presetY(), sx + 18, presetY() + 12, 0xFF000000 | PRESETS[i]);
        }

        // RGB sliders
        String[] labels = {"R", "G", "B"};
        for (int i = 0; i < 3; i++) {
            int sy = sliderY(i), sx = x + 22, sw = SET_W - 30;
            ctx.drawTextWithShadow(textRenderer, labels[i], x + 8, sy + 1, OFF);
            ctx.fill(sx, sy + 3, sx + sw, sy + 7, 0xFF2A2A30);
            int fill = channel(i) * sw / 255;
            ctx.fill(sx, sy + 3, sx + fill, sy + 7, Settings.accent());
            ctx.fill(sx + fill - 2, sy, sx + fill + 2, sy + 10, 0xFFFFFFFF);
        }

        // toggles
        drawToggle(ctx, mx, my, 0, "Name Protect", Settings.nameProtect);
        drawToggle(ctx, mx, my, 1, "Arraylist", Settings.arraylist);
        drawToggle(ctx, mx, my, 2, "Dim background", Settings.dimBackground);
        ctx.drawTextWithShadow(textRenderer, "Display name (client-side only)", x + 8, toggleY(3) - 6 + 2, OFF);

        nameField.setVisible(true);
        nameField.render(ctx, mx, my, delta);
    }

    private void renderConfigs(DrawContext ctx, int mx, int my, float delta) {
        int x = cfgX();
        int h = HEADER_H + 24 + Math.max(1, configs.size()) * CFG_ROW + 6;
        ctx.fill(x, TOP, x + CFG_W, TOP + h, BG);
        ctx.fill(x, TOP, x + CFG_W, TOP + HEADER_H, HEADER);
        ctx.fill(x, TOP + HEADER_H - 1, x + CFG_W, TOP + HEADER_H, Settings.accent());
        ctx.drawTextWithShadow(textRenderer, "Configs", x + 6, TOP + 4, 0xFFFFFFFF);
        configNameField.setVisible(true);
        configNameField.render(ctx, mx, my, delta);
        ctx.drawTextWithShadow(textRenderer, "[Save]", x + CFG_W - 40, TOP + HEADER_H + 8, Settings.accent());
        int ry = TOP + HEADER_H + 24;
        for (String name : configs) {
            if (in(mx, my, x, ry, CFG_W - 24, CFG_ROW)) ctx.fill(x, ry, x + CFG_W - 24, ry + CFG_ROW, HOVER);
            ctx.drawTextWithShadow(textRenderer, name, x + 8, ry + 3, OFF);
            ctx.drawTextWithShadow(textRenderer, "x", x + CFG_W - 16, ry + 3, 0xFFB05050);
            ry += CFG_ROW;
        }
    }

    private void drawToggle(DrawContext ctx, int mx, int my, int i, String label, boolean on) {
        int x = setX(), y = toggleY(i);
        if (in(mx, my, x, y, SET_W, ROW_H)) ctx.fill(x, y, x + SET_W, y + ROW_H, HOVER);
        if (on) ctx.fill(x, y, x + 2, y + ROW_H, Settings.accent());
        ctx.drawTextWithShadow(textRenderer, label, x + 8, y + 3, on ? Settings.accent() : OFF);
    }

    // ---- input (1.21.9+ passes Click objects) ---------------------------
    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();

        for (int i = 0; i < 3; i++) {
            if (in(mx, my, tabX(i), TAB_Y, TAB_W, TAB_H)) {
                tab = i;
                nameField.setVisible(tab == 1);
                configNameField.setVisible(tab == 2);
                if (tab != 1) nameField.setFocused(false);
                if (tab != 2) configNameField.setFocused(false);
                if (tab == 2) refreshConfigs();
                return true;
            }
        }

        if (tab == 2) {
            int x = cfgX();
            if (in(mx, my, x + CFG_W - 44, TOP + HEADER_H + 8, 38, 10)) {
                String n = configNameField.getText().isBlank() ? "default" : configNameField.getText().trim();
                Config.save(n);
                refreshConfigs();
                return true;
            }
            int ry = TOP + HEADER_H + 24;
            for (String name : configs) {
                if (in(mx, my, x + CFG_W - 20, ry, 16, CFG_ROW)) { Config.delete(name); refreshConfigs(); return true; }
                if (in(mx, my, x, ry, CFG_W - 24, CFG_ROW)) { Config.load(name); return true; }
                ry += CFG_ROW;
            }
            return super.mouseClicked(click, doubled);
        }
        if (tab == 0) {
            int i = 0;
            for (Module.Category cat : Module.Category.values()) {
                int x = panelX(i++), ry = TOP + HEADER_H;
                for (Module m : Client.MODULES.in(cat)) {
                    if (in(mx, my, x, ry, PANEL_W, ROW_H)) {
                        if (click.button() == 1 && m.modes.length > 0) m.cycleMode();
                        else if (click.button() == 0) m.toggle();
                        return true;
                    }
                    ry += ROW_H;
                }
            }
        } else {
            int x = setX();
            for (int i = 0; i < PRESETS.length; i++) {
                if (in(mx, my, x + 8 + i * 22, presetY(), 18, 12)) {
                    Settings.r = PRESETS[i] >> 16 & 255;
                    Settings.g = PRESETS[i] >> 8 & 255;
                    Settings.b = PRESETS[i] & 255;
                    return true;
                }
            }
            for (int i = 0; i < 3; i++) {
                if (in(mx, my, x + 22, sliderY(i), SET_W - 30, 10)) {
                    dragging = i;
                    dragSlider(mx);
                    return true;
                }
            }
            for (int i = 0; i < 3; i++) {
                if (in(mx, my, x, toggleY(i), SET_W, ROW_H)) {
                    if (i == 0) Settings.nameProtect = !Settings.nameProtect;
                    if (i == 1) Settings.arraylist = !Settings.arraylist;
                    if (i == 2) Settings.dimBackground = !Settings.dimBackground;
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging >= 0) { dragSlider(click.x()); return true; }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        dragging = -1;
        return super.mouseReleased(click);
    }

    @Override
    public void removed() {
        Settings.save();
        if (client != null)
            GLFW.glfwSetInputMode(client.getWindow().getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
    }

    @Override
    public boolean shouldPause() { return false; }
}
