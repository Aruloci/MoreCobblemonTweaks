package me.justahuman.more_cobblemon_tweaks.features;

import me.justahuman.more_cobblemon_tweaks.config.ModConfig;
import me.justahuman.more_cobblemon_tweaks.features.egg.EnhancedEggLore;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static net.minecraft.ChatFormatting.*;

public class LoreEnhancements {
    private static final String BASE_KEY = "more_cobblemon_tweaks.lore_enhancements.";

    public static void enhanceEggLore(List<Component> lore, List<Component> newLore, EnhancedEggLore enhancedEggLore) {
        Component name = enhancedEggLore.getName(lore);
        final boolean shiny = enhancedEggLore.isShiny();


        if (ModConfig.isEnabled("shiny_egg_indicator") && shiny) {
            name = name.copy().append(Component.literal(" ★").withStyle(YELLOW));
        }

        if (ModConfig.isEnabled("perfect_iv_egg_indicator") && enhancedEggLore.hasPerfectIVs()) {
            name = name.copy().append(Component.literal(" 🛆").withStyle(YELLOW));
        }

        final String gender = enhancedEggLore.getGender();
        if (gender.equals("MALE") || gender.equals("FEMALE")) {
            boolean male = gender.equals("MALE");
            name = name.copy().append(Component.literal(male ? " ♂" : " ♀")
                    .withStyle(style -> style.withColor(male ? 0x32CBFF : 0xFC5454)));
        }
        lore.set(0, name);

        final List<Component> hatchProgress = enhancedEggLore.getHatchProgress(lore);
        boolean spacer = false;

        if (hatchProgress != null && !hatchProgress.isEmpty()) {
            newLore.addAll(hatchProgress);
            spacer = true;
        }

        String nature = enhancedEggLore.getNature();
        String abilityName = enhancedEggLore.getAbility();
        String form = enhancedEggLore.getForm();
        if ((nature != null || abilityName != null || form != null) && spacer) {
            newLore.add(Component.literal(" "));
            spacer = false;
        }

        if (nature != null) {
            if (nature.contains(":")) {
                nature = StringUtils.capitalize(nature.substring(nature.indexOf(':') + 1));
            }
            newLore.add(translate("egg.nature").withStyle(YELLOW)
                    .append(Component.literal(nature).withStyle(WHITE)));
            spacer = true;
        }

        if (abilityName != null) {
            newLore.add(translate("egg.ability").withStyle(GOLD)
                    .append(Component.literal(StringUtils.capitalize(abilityName)).withStyle(WHITE)));
            spacer = true;
        }

        if (form != null) {
            newLore.add(translate("egg.form").withStyle(WHITE)
                    .append(Component.literal(StringUtils.capitalize(form))));
            spacer = true;
        }

        if (enhancedEggLore.hasIVs()) {
            if (spacer) {
                newLore.add(Component.literal(" "));
            }

            addIvLine(newLore, "egg.iv.hp", enhancedEggLore.getHpIV(), ChatFormatting.GREEN);
            addIvLine(newLore, "egg.iv.attack", enhancedEggLore.getAtkIV(), ChatFormatting.RED);
            addIvLine(newLore, "egg.iv.defense", enhancedEggLore.getDefIV(), ChatFormatting.GOLD);
            addIvLine(newLore, "egg.iv.sp_attack", enhancedEggLore.getSpAtkIV(), ChatFormatting.LIGHT_PURPLE);
            addIvLine(newLore, "egg.iv.sp_defense", enhancedEggLore.getSpDefIV(), ChatFormatting.YELLOW);
            addIvLine(newLore, "egg.iv.speed", enhancedEggLore.getSpeedIV(), ChatFormatting.AQUA);
        }
    }

    public static MutableComponent translate(String key, Object... args) {
        return Component.translatable(BASE_KEY + key, args);
    }

    private static ChatFormatting colorForIv(Integer iv) {
        if (iv == null || iv < 0) return ChatFormatting.DARK_GRAY;
        if (iv == 31) return BOLD;
        return ChatFormatting.WHITE;
    }

    private static void addIvLine(List<Component> out, String i18nKey, Integer iv, ChatFormatting labelColor) {
        if (iv == null || iv < 0) return;
        out.add(
                translate(i18nKey).withStyle(labelColor)
                        .append(Component.literal(Integer.toString(iv)).withStyle(colorForIv(iv)))
        );
    }
}
