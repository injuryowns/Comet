package com.example.client.gui;

import com.example.client.Client;
import com.example.client.Settings;
import com.example.client.config.Config;
import com.example.client.config.Setting;
import com.example.client.modules.Cursor;
import com.example.client.modules.Module;
import com.example.client.util.ColorUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Compact, responsive click GUI.
 * Left click toggles modules; right click opens module settings.
 */
public class ClickGui extends Screen {
    private static final int TOP = 42;
    private static final int NAV_H = 26;
    private static final int CARD_W = 150;
    private static final int ROW_H = 20;
    private static final int GAP = 10;
    private static final int PAD = 10;
    private static final int PANEL_W = 230;

    private static final int BG = 0xF20D0F13;
    private static final int SURFACE = 0xF5181B21;
    private static final int SURFACE_2 = 0xF71D2028;
    private static final int HEADER = 0xFF11141A;
    private static final int BORDER = 0x301FFFFFF;
    private static final int HOVER = 0x18FFFFFF;
    private static final int MUTED = 0xFF858B98;
    private static final int WHITE = 0xFFE9ECF2;
    private static final int SHADOW = 0x50000000;

    private static final int[] PRESETS = {
            0x2BD98A, 0x3D8BFF, 0xA45CFF, 0xFF5CA8, 0xFF4D4D, 0xFF9F1C
    };

    private int tab = 0;
    private int dragging = -1;
    private int panelSettingDrag = -1;
    private Module panelModule;
    private int panelX, panelY;
    private TextFieldWidget nameField;
    private TextFieldWidget configNameField;
    private final List<String> configs = new ArrayList<>();

    public ClickGui() {
        super(Text.literal("Comet"));
    }

    @Override
    protected void init() {
        nameField = new TextFieldWidget(textRenderer, settingsX() + 10, 0, 210, 20, Text.literal("Display name"));
        nameField.setMaxLength(16);
        nameField.setText(Settings.fakeName);
        nameField.setChangedListener(s -> Settings.fakeName = s);
        addDrawableChild(nameField);

        configNameField = new TextFieldWidget(textRenderer, configsX() + 10, 0, 180, 20, Text.literal("Config name"));
        configNameField.setMaxLength(24);
        configNameField.setText("default");
        addDrawableChild(configNameField);

        refreshVisibility();
        refreshConfigs();

        if (Client.MODULES.isOn(Cursor.class)) {
            GLFW.glfwSetInputMode(client.getWindow().getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
        }
    }

    private void refreshVisibility() {
        if (nameField != null) nameField.setVisible(tab == 1);
        if (configNameField != null) configNameField.setVisible(tab == 2);
        if (tab != 1 && nameField != null) nameField.setFocused(false);
        if (tab != 2 && configNameField != null) configNameField.setFocused(false);
    }

    private void refreshConfigs() {
        configs.clear();
        configs.addAll(Config.list());
    }

    private int navX(int i) {
        int total = 3 * 92 + 2 * 6;
        return (width - total) / 2 + i * 98;
    }

    private int modulesX() {
        int count = Module.Category.values().length;
        int total = count * CARD_W + (count - 1) * GAP;
        return Math.max(PAD, (width - total) / 2);
    }

    private int settingsX() {
        return Math.max(PAD, (width - 250) / 2);
    }

    private int configsX() {
        return Math.max(PAD, (width - 210) / 2);
    }

    private int categoryX(int index) {
        return modulesX() + index * (CARD_W + GAP);
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private int channel(int i) {
        return i == 0 ? Settings.r : i == 1 ? Settings.g : Settings.b;
    }

    private void setChannel(int i, int v) {
        v = Math.max(0, Math.min(255, v));
        if (i == 0) Settings.r = v;
        else if (i == 1) Settings.g = v;
        else Settings.b = v;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, BG);
        renderTopBar(ctx);
        if (tab == 0) renderModules(ctx, mx, my);
        else if (tab == 1) renderSettings(ctx, mx, my, delta);
        else renderConfigs(ctx, mx, my, delta);

        if (panelModule != null) renderModulePanel(ctx, mx, my);
        if (Client.MODULES.isOn(Cursor.class)) Cursor.drawGuiCursor(ctx, mx, my);
    }

    private void renderTopBar(DrawContext ctx) {
        ctx.fill(0, 0, width, 32, HEADER);
        ctx.fill(0, 31, width, 32, Settings.accent());

        ctx.drawTextWithShadow(textRenderer, "Comet", 12, 10, ColorUtil.accent(0.15f));

        String[] names = {"Modules", "Settings", "Configs"};
        for (int i = 0; i < 3; i++) {
            int x = navX(i);
            boolean selected = i == tab;
            ctx.fill(x, 4, x + 92, 28, selected ? SURFACE_2 : HEADER);
            if (selected) ctx.fill(x, 26, x + 92, 28, Settings.accent());
            int tw = textRenderer.getWidth(names[i]);
            ctx.drawTextWithShadow(textRenderer, names[i], x + (92 - tw) / 2, 11,
                    selected ? WHITE : MUTED);
        }

        int active = (int) Client.MODULES.modules.stream().filter(Module::isEnabled).count();
        String status = active + " active";
        int tw = textRenderer.getWidth(status);
        ctx.drawTextWithShadow(textRenderer, status, width - tw - 12, 10, MUTED);
    }

    private void renderModules(DrawContext ctx, int mx, int my) {
        int i = 0;
        for (Module.Category cat : Module.Category.values()) {
            int x = categoryX(i++);
            int y = TOP;
            List<Module> mods = Client.MODULES.in(cat);
            int h = 34 + Math.max(1, mods.size()) * ROW_H;

            drawCard(ctx, x, y, CARD_W, h);
            ctx.drawTextWithShadow(textRenderer, title(cat.name()), x + 10, y + 9, WHITE);

            int enabled = (int) mods.stream().filter(Module::isEnabled).count();
            String count = enabled + "/" + mods.size();
            int cw = textRenderer.getWidth(count);
            ctx.drawTextWithShadow(textRenderer, count, x + CARD_W - cw - 10, y + 9, MUTED);

            int ry = y + 30;
            for (Module m : mods) {
                boolean hover = in(mx, my, x + 1, ry, CARD_W - 2, ROW_H);
                if (hover) ctx.fill(x + 1, ry, x + CARD_W - 1, ry + ROW_H, HOVER);

                int accent = ColorUtil.accent((ry - TOP) * 0.012f);
                if (m.isEnabled()) {
                    ctx.fill(x + 1, ry, x + 3, ry + ROW_H, accent);
                }

                ctx.drawTextWithShadow(textRenderer, m.name, x + 10, ry + 6,
                        m.isEnabled() ? accent : MUTED);

                if (m.hasPanel()) {
                    ctx.drawTextWithShadow(textRenderer, "›", x + CARD_W - 12, ry + 5, MUTED);
                }
                ry += ROW_H;
            }
        }
    }

    private void drawCard(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x + 3, y + 3, x + w + 3, y + h + 3, SHADOW);
        ctx.fill(x, y, x + w, y + h, SURFACE);
        ctx.fill(x, y, x + w, y + 1, Settings.accent());
        ctx.fill(x, y, x + 1, y + h, BORDER);
        ctx.fill(x + w - 1, y, x + w, y + h, BORDER);
        ctx.fill(x, y + h - 1, x + w, y + h, BORDER);
    }

    private void renderSettings(DrawContext ctx, int mx, int my, float delta) {
        int x = settingsX(), y = TOP, w = 250, h = 270;
        drawCard(ctx, x, y, w, h);
        ctx.drawTextWithShadow(textRenderer, "Client settings", x + 10, y + 9, WHITE);

        ctx.drawTextWithShadow(textRenderer, "Accent", x + 10, y + 38, MUTED);
        for (int i = 0; i < PRESETS.length; i++) {
            int sx = x + 10 + i * 28;
            int color = 0xFF000000 | PRESETS[i];
            ctx.fill(sx, y + 53, sx + 22, y + 69, color);
            if ((Settings.r << 16 | Settings.g << 8 | Settings.b) == PRESETS[i]) {
                ctx.fill(sx - 1, y + 52, sx + 23, y + 53, WHITE);
                ctx.fill(sx - 1, y + 69, sx + 23, y + 70, WHITE);
            }
        }

        String[] labels = {"R", "G", "B"};
        for (int i = 0; i < 3; i++) {
            int sy = y + 86 + i * 23;
            int sx = x + 28, sw = w - 40;
            ctx.drawTextWithShadow(textRenderer, labels[i], x + 10, sy + 3, MUTED);
            ctx.fill(sx, sy + 6, sx + sw, sy + 10, 0xFF2A2E37);
            int fill = channel(i) * sw / 255;
            if (fill > 0) ctx.fill(sx, sy + 6, sx + fill, sy + 10, Settings.accent());
            ctx.fill(Math.max(sx, sx + fill - 2), sy + 3, Math.min(sx + sw, sx + fill + 2), sy + 13, WHITE);
        }

        drawSettingToggle(ctx, mx, my, x, y + 160, "Name Protect", Settings.nameProtect);
        drawSettingToggle(ctx, mx, my, x, y + 184, "Arraylist", Settings.arraylist);
        drawSettingToggle(ctx, mx, my, x, y + 208, "Dim background", Settings.dimBackground);

        ctx.drawTextWithShadow(textRenderer, "Display name", x + 10, y + 236, MUTED);
        nameField.setPosition(x + 10, y + 249);
        nameField.setWidth(w - 20);
        nameField.render(ctx, mx, my, delta);
    }

    private void drawSettingToggle(DrawContext ctx, int mx, int my, int x, int y, String label, boolean on) {
        if (in(mx, my, x + 6, y, 238, 20)) ctx.fill(x + 6, y, x + 244, y + 20, HOVER);
        ctx.drawTextWithShadow(textRenderer, label, x + 10, y + 6, on ? WHITE : MUTED);
        int tx = x + 214;
        ctx.fill(tx, y + 4, tx + 28, y + 16, on ? Settings.accent() : 0xFF343842);
        ctx.fill(on ? tx + 17 : tx + 3, y + 6, on ? tx + 25 : tx + 11, y + 14, WHITE);
    }

    private void renderConfigs(DrawContext ctx, int mx, int my, float delta) {
        int x = configsX(), y = TOP, w = 210;
        int h = 58 + Math.max(1, configs.size()) * 22;
        drawCard(ctx, x, y, w, h);
        ctx.drawTextWithShadow(textRenderer, "Configs", x + 10, y + 9, WHITE);

        configNameField.setPosition(x + 10, y + 30);
        configNameField.setWidth(130);
        configNameField.render(ctx, mx, my, delta);

        if (in(mx, my, x + 145, y + 30, 55, 20)) ctx.fill(x + 145, y + 30, x + 200, y + 50, HOVER);
        ctx.drawTextWithShadow(textRenderer, "Save", x + 158, y + 36, Settings.accent());

        int ry = y + 58;
        for (String name : configs) {
            boolean hover = in(mx, my, x + 5, ry, w - 10, 20);
            if (hover) ctx.fill(x + 5, ry, x + w - 5, ry + 20, HOVER);
            ctx.drawTextWithShadow(textRenderer, name, x + 10, ry + 6, MUTED);
            ctx.drawTextWithShadow(textRenderer, "×", x + w - 20, ry + 5, 0xFFB65B65);
            ry += 22;
        }
    }

    private void renderModulePanel(DrawContext ctx, int mx, int my) {
        Module m = panelModule;
        int rows = Math.max(1, (m.modes.length > 0 ? 1 : 0) + m.settings.size());
        int h = 34 + rows * 23;
        int px = Math.max(8, Math.min(panelX, width - PANEL_W - 8));
        int py = Math.max(8, Math.min(panelY, height - h - 8));

        drawCard(ctx, px, py, PANEL_W, h);
        ctx.drawTextWithShadow(textRenderer, m.name, px + 10, py + 9, WHITE);

        int ry = py + 31;
        if (m.modes.length > 0) {
            ctx.drawTextWithShadow(textRenderer, "Mode", px + 10, ry + 4, MUTED);
            ctx.drawTextWithShadow(textRenderer, m.modeName(), px + PANEL_W - textRenderer.getWidth(m.modeName()) - 10, ry + 4, Settings.accent());
            ry += 23;
        }

        for (Setting<?> st : m.settings) {
            if (st instanceof Setting.BoolSetting bs) {
                ctx.drawTextWithShadow(textRenderer, bs.display(), px + 10, ry + 5, bs.get() ? WHITE : MUTED);
                ctx.drawTextWithShadow(textRenderer, bs.get() ? "ON" : "OFF", px + PANEL_W - 30, ry + 5,
                        bs.get() ? Settings.accent() : MUTED);
            } else if (st instanceof Setting.IntSetting is) {
                ctx.drawTextWithShadow(textRenderer, is.display(), px + 10, ry + 2, MUTED);
                int sx = px + 10, sw = PANEL_W - 20, sy = ry + 15;
                ctx.fill(sx, sy, sx + sw, sy + 3, 0xFF2A2E37);
                int fill = (int) (sw * is.fraction());
                if (fill > 0) ctx.fill(sx, sy, sx + fill, sy + 3, Settings.accent());
            }
            ry += 23;
        }
    }

    private static String title(String s) {
        return s.substring(0, 1) + s.substring(1).toLowerCase();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();

        if (panelModule != null) {
            if (handlePanelClick(click)) return true;
            panelModule = null;
        }

        for (int i = 0; i < 3; i++) {
            if (in(mx, my, navX(i), 4, 92, 24)) {
                tab = i;
                refreshVisibility();
                if (tab == 2) refreshConfigs();
                return true;
            }
        }

        if (tab == 0 && click.button() <= 1) {
            int ci = 0;
            for (Module.Category cat : Module.Category.values()) {
                int x = categoryX(ci++);
                int ry = TOP + 30;
                for (Module m : Client.MODULES.in(cat)) {
                    if (in(mx, my, x, ry, CARD_W, ROW_H)) {
                        if (click.button() == 1) {
                            panelModule = m.hasPanel() ? m : null;
                            if (panelModule != null) {
                                int rows = Math.max(1, (m.modes.length > 0 ? 1 : 0) + m.settings.size());
                                int h = 34 + rows * 23;
                                panelX = Math.max(8, Math.min((int) mx + 12, width - PANEL_W - 8));
                                panelY = Math.max(8, Math.min((int) my + 8, height - h - 8));
                            }
                        } else {
                            m.toggle();
                        }
                        return true;
                    }
                    ry += ROW_H;
                }
            }
        } else if (tab == 1) {
            handleSettingsClick(mx, my);
        } else if (tab == 2) {
            handleConfigClick(mx, my);
        }
        return super.mouseClicked(click, doubled);
    }

    private void handleSettingsClick(double mx, double my) {
        int x = settingsX(), y = TOP;
        for (int i = 0; i < PRESETS.length; i++) {
            if (in(mx, my, x + 10 + i * 28, y + 53, 22, 16)) {
                Settings.r = PRESETS[i] >> 16 & 255;
                Settings.g = PRESETS[i] >> 8 & 255;
                Settings.b = PRESETS[i] & 255;
                return;
            }
        }
        for (int i = 0; i < 3; i++) {
            if (in(mx, my, x + 28, y + 83 + i * 23, 210, 17)) {
                dragging = i;
                dragSlider(mx);
                return;
            }
        }
        if (in(mx, my, x + 6, y + 160, 238, 20)) Settings.nameProtect = !Settings.nameProtect;
        else if (in(mx, my, x + 6, y + 184, 238, 20)) Settings.arraylist = !Settings.arraylist;
        else if (in(mx, my, x + 6, y + 208, 238, 20)) Settings.dimBackground = !Settings.dimBackground;
    }

    private void handleConfigClick(double mx, double my) {
        int x = configsX(), y = TOP;
        if (in(mx, my, x + 145, y + 30, 55, 20)) {
            String n = configNameField.getText().isBlank() ? "default" : configNameField.getText().trim();
            Config.save(n);
            refreshConfigs();
            return;
        }
        int ry = y + 58;
        for (String name : List.copyOf(configs)) {
            if (in(mx, my, x + 5, ry, 185, 20)) {
                if (in(mx, my, x + 185, ry, 20, 20)) {
                    Config.delete(name);
                    refreshConfigs();
                } else {
                    Config.load(name);
                }
                return;
            }
            ry += 22;
        }
    }

    private boolean handlePanelClick(Click click) {
        Module m = panelModule;
        double mx = click.x(), my = click.y();
        int rows = Math.max(1, (m.modes.length > 0 ? 1 : 0) + m.settings.size());
        int h = 34 + rows * 23;
        int px = Math.max(8, Math.min(panelX, width - PANEL_W - 8));
        int py = Math.max(8, Math.min(panelY, height - h - 8));

        if (!in(mx, my, px, py, PANEL_W, h)) return false;

        int ry = py + 31;
        if (m.modes.length > 0) {
            if (in(mx, my, px, ry, PANEL_W, 23)) {
                if (click.button() == 0) m.cycleMode();
                return true;
            }
            ry += 23;
        }

        for (int i = 0; i < m.settings.size(); i++) {
            Setting<?> st = m.settings.get(i);
            if (st instanceof Setting.BoolSetting bs && in(mx, my, px, ry, PANEL_W, 23)) {
                if (click.button() == 0) bs.toggle();
                return true;
            }
            if (st instanceof Setting.IntSetting is && in(mx, my, px + 10, ry + 10, PANEL_W - 20, 10)) {
                if (click.button() == 0) {
                    panelSettingDrag = i;
                    dragPanelSetting(is, px, mx);
                }
                return true;
            }
            ry += 23;
        }
        return true;
    }

    private void dragSlider(double mx) {
        int x = settingsX() + 28, sw = 210;
        setChannel(dragging, (int) Math.round((mx - x) / (double) sw * 255));
    }

    private void dragPanelSetting(Setting.IntSetting setting, int px, double mx) {
        setting.setFraction((mx - (px + 10)) / (double) (PANEL_W - 20));
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (panelSettingDrag >= 0 && panelModule != null) {
            Setting<?> st = panelModule.settings.get(panelSettingDrag);
            if (st instanceof Setting.IntSetting is) {
                dragPanelSetting(is, px, click.x());
            }
            return true;
        }
        if (dragging >= 0) {
            dragSlider(click.x());
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        dragging = -1;
        panelSettingDrag = -1;
        return super.mouseReleased(click);
    }

    @Override
    public void removed() {
        Settings.save();
        if (client != null) {
            GLFW.glfwSetInputMode(client.getWindow().getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
