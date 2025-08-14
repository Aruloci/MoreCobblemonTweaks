package me.justahuman.more_cobblemon_tweaks;

import me.justahuman.more_cobblemon_tweaks.config.ConfigScreen;
import me.justahuman.more_cobblemon_tweaks.config.ModConfig;
import me.justahuman.more_cobblemon_tweaks.features.Keybinds;
import me.justahuman.more_cobblemon_tweaks.features.egg.CobbreedingIntegration;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public final class MoreCobblemonTweaksFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MoreCobblemonTweaks.initClient(
                FabricLoader.getInstance().getConfigDir().resolve(MoreCobblemonTweaks.MOD_ID + ".json").toFile(),
                id -> FabricLoader.getInstance().isModLoaded(id),
                id -> FabricLoader.getInstance().getModContainer(id)
                        .map(container -> container.getMetadata().getVersion().getFriendlyString())
                        .orElse("unknown")
        );

        KeyBindingHelper.registerKeyBinding(Keybinds.OPEN_CONFIG);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (Keybinds.OPEN_CONFIG.consumeClick() && FabricLoader.getInstance().isModLoaded("cloth-config2")) {
                client.setScreen(ConfigScreen.buildScreen(client.screen));
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ModConfig.clearServerConfig());

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                MoreCobblemonTweaks.onReload(manager);
            }

            @Override
            public ResourceLocation getFabricId() {
                return MoreCobblemonTweaks.id("reload_listener");
            }
        });

        registerEggState();
    }

    private static void registerEggState() {
        var eggId = ResourceLocation.fromNamespaceAndPath("cobbreeding", "pokemon_egg");
        var egg   = BuiltInRegistries.ITEM.get(eggId);

        ItemProperties.register(egg, MoreCobblemonTweaks.id("egg_state"),
                (stack, level, entity, seed) -> {
                    var ci = CobbreedingIntegration.get(stack);
                    if (ci == null) return 0.0F;

                    // 0.0 = normal, 0.1 = shiny, 0.2 = perfect, 0.3 = shiny+perfect
                    float v = 0.0f;
                    if (ci.isShiny())       v += 0.1f;
                    if (ci.hasPerfectIVs()) v += 0.2f;

                    return v;
                }
        );
    }


}
