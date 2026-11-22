package com.robloxcloudly;

import com.robloxcloudly.game.Cmds;
import com.robloxcloudly.game.GameManager;
import com.robloxcloudly.game.Items2;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

/** robloxcloudly.gg – mod na serwer Fabric 1.21.11 (gracze nie muszą nic instalować). */
public class RobloxCloudly implements ModInitializer {
    public static final String MODID = "robloxcloudly";

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(GameManager::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> GameManager.onServerStopping());
        ServerTickEvents.END_SERVER_TICK.register(GameManager::tick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> GameManager.onJoin(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> GameManager.onDisconnect(handler.getPlayer()));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> GameManager.onRespawn(newPlayer));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Cmds.register(dispatcher));

        // gracze w lobby i na mapach nie dostają obrażeń
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) ->
                !(entity instanceof ServerPlayer sp && GameManager.isOurs(sp.level())));

        // PPM w encję: skin w lobby, hoverboard, jajka
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!(player instanceof ServerPlayer sp) || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            return GameManager.onInteract(sp, entity) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        });

        // PPM przedmiotem: menu, hoverboard; jajka nie da się postawić
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
            ItemStack stack = player.getItemInHand(hand);
            String kind = Items2.kind(stack);
            switch (kind) {
                case "menu" -> {
                    if (GameManager.mapOf(sp.getUUID()) != null) com.robloxcloudly.game.Menus.openMain(sp);
                    else com.robloxcloudly.game.Menus.openServers(sp, 0);
                    return InteractionResult.SUCCESS;
                }
                case "hoverboard" -> {
                    GameManager.toggleHover(sp);
                    return InteractionResult.SUCCESS;
                }
                case "egg" -> { return InteractionResult.FAIL; }
                default -> { return InteractionResult.PASS; }
            }
        });
    }
}
