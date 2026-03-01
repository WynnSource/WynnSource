package fyw.fyi.wynnsource.mixin.client.mc;

import fyw.fyi.wynnsource.event.EventBus;
import fyw.fyi.wynnsource.event.RemoteContainerScreenEvent;
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
public abstract class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onInventory", at = @At("TAIL"))
    private void handleContainerContentPost(InventoryS2CPacket packet, CallbackInfo ci) {
        PlayerEntity playerEntity = MinecraftClient.getInstance().player;
        if (playerEntity != null && packet.syncId() == playerEntity.currentScreenHandler.syncId) {
            if (playerEntity.currentScreenHandler instanceof GenericContainerScreenHandler) {
                EventBus.emitSync(
                        new RemoteContainerScreenEvent((GenericContainerScreen) MinecraftClient.getInstance().currentScreen)
                );
            }
        }
    }
}
