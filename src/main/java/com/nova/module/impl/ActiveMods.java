package com.nova.module.impl;

import com.nova.NovaClient;
import com.nova.font.Fonts;
import com.nova.module.Category;
import com.nova.module.HudModule;
import com.nova.module.Module;
import com.nova.setting.BooleanSetting;
import com.nova.setting.ModeSetting;
import com.nova.util.Anim;
import com.nova.util.Render;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Lists every mod that's turned on, longest name first. Mods slide in and out when toggled. */
public class ActiveMods extends HudModule {
    private static final float LINE_H = 11f;
    private static final float PAD = 4f;
    private static final float BAR = 1.5f;

    private final ModeSetting align = add(new ModeSetting("Align", "Auto", "Auto", "Left", "Right"));
    private final ModeSetting color = add(new ModeSetting("Color", "Accent", "Accent", "White", "Chroma"));
    private final BooleanSetting background = add(new BooleanSetting("Background", true));
    private final BooleanSetting accentBar = add(new BooleanSetting("Side bar", true));
    private final BooleanSetting hideHudMods = add(new BooleanSetting("Hide HUD mods", false));

    private final Map<Module, Anim> slide = new HashMap<>();

    public ActiveMods() {
        super("Active Mods", "Shows a list of the mods you have on", Category.HUD, 0, 0);
        setDefaultPosition(Anchor.END, Anchor.START, 0, 2);
    }

    @Override
    public boolean showInModList() {
        return false;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.writable_book);
    }

    /** Mods currently shown or still sliding out, longest first, with their slide amount. */
    private List<Module> entries() {
        List<Module> result = new ArrayList<>();
        for (Module module : NovaClient.getModuleManager().getModules()) {
            if (!module.showInModList()) continue;
            boolean visible = module.isEnabled() && !(hideHudMods.isOn() && module.getCategory() == Category.HUD);
            Anim anim = slide.get(module);
            if (anim == null) {
                anim = new Anim(12f, visible ? 1f : 0f);
                slide.put(module, anim);
            }
            anim.setTarget(visible ? 1f : 0f);
            if (anim.get() > 0.01f) result.add(module);
        }
        result.sort((a, b) -> Float.compare(Fonts.REGULAR.getWidth(b.getName()), Fonts.REGULAR.getWidth(a.getName())));
        return result;
    }

    private boolean alignRight() {
        if (align.is("Right")) return true;
        if (align.is("Left")) return false;
        return getAnchorX() == Anchor.END;
    }

    @Override
    public int getWidth() {
        float max = 50;
        for (Module module : entries()) max = Math.max(max, Fonts.REGULAR.getWidth(module.getName()));
        return (int) Math.ceil(max + PAD * 2 + BAR);
    }

    @Override
    public int getHeight() {
        float h = 0;
        for (Module module : entries()) h += LINE_H * slide.get(module).get();
        return (int) Math.max(LINE_H, Math.ceil(h));
    }

    @Override
    public void render(boolean preview) {
        List<Module> entries = entries();
        if (entries.isEmpty()) {
            if (preview) Fonts.REGULAR.drawWithShadow("Active Mods", PAD, 1, 0xFFFFFFFF);
            return;
        }
        int width = getWidth();
        boolean right = alignRight();
        float y = 0;
        for (int i = 0; i < entries.size(); i++) {
            Module module = entries.get(i);
            float t = slide.get(module).get();
            String name = module.getName();
            float lineW = Fonts.REGULAR.getWidth(name) + PAD * 2 + BAR;
            float hiddenShift = (1f - t) * (lineW + 4);
            float x = right ? width - lineW + hiddenShift : -hiddenShift;
            int textColor = Render.alpha(lineColor(i), t);

            if (background.isOn()) Render.rect(x, y, lineW, LINE_H * t, Render.alpha(0x80000000, t));
            if (accentBar.isOn()) Render.rect(right ? x + lineW - BAR : x, y, BAR, LINE_H * t, textColor);
            float textX = right ? x + PAD : x + PAD + BAR;
            Fonts.REGULAR.drawWithShadow(name, textX, y + (LINE_H - Fonts.REGULAR.getHeight()) / 2f, textColor);
            y += LINE_H * t;
        }
    }

    private int lineColor(int index) {
        if (color.is("White")) return 0xFFFFFFFF;
        if (color.is("Chroma")) return Render.chroma(index * 0.06f);
        return NovaClient.accent();
    }
}
