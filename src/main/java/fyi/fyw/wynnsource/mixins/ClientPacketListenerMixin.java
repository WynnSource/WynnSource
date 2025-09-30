package fyi.fyw.wynnsource.mixins;

import fyi.fyw.wynnsource.WynnSourceEntry;
import fyi.fyw.wynnsource.module.RewardPoolCollector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "onInventory", at = @At("TAIL"))
    private void handleContainerContentPost(InventoryS2CPacket packet, CallbackInfo ci) {
        PlayerEntity playerEntity = MinecraftClient.getInstance().player;
        if (packet.getSyncId() == playerEntity.currentScreenHandler.syncId) {
            if (playerEntity.currentScreenHandler instanceof GenericContainerScreenHandler)
                try {
                    RewardPoolCollector.INSTANCE.onContainerScreen(
                            (GenericContainerScreen) MinecraftClient.getInstance().currentScreen
                    );
                } catch (Exception e) {
                    // If any error occurs, we don't want to crash the game,
                    // but we want to log it
                    WynnSourceEntry.INSTANCE.getLOGGER().error("[WynnSource] Error while handling container screen: {}", e.getMessage());
                    e.printStackTrace();
                }

        }
    }
}
